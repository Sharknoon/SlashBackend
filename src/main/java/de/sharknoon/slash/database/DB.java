package de.sharknoon.slash.database;

import com.mongodb.*;
import com.mongodb.client.*;
import de.sharknoon.slash.networking.endpoints.login.LoginMessage;
import de.sharknoon.slash.networking.endpoints.register.RegisterMessage;
import de.sharknoon.slash.properties.*;
import org.bson.Document;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Collections;
import java.util.logging.*;

import static com.mongodb.client.model.Filters.*;

public class DB {
    
    private static MongoDatabase database;
    private static MongoCollection<Document> users;
    
    static {
        DBConfig props = Properties.getProperties();
        
        String ip = props.databaseip();
        int port = props.databaseport();
        String database = props.database();
        String username = props.dbuser();
        String password = props.dbpassword();
        
        String usersCollection = Values.USERS_COLLECTION;
        
        MongoCredential credential = MongoCredential.createCredential(username, database, password.toCharArray());
        MongoClient mongoClient = MongoClients.create(
                MongoClientSettings
                        .builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(Collections.singletonList(new ServerAddress(ip, port)))
                        )
                        .credential(credential)
                        .build()
        );
        
        DB.database = mongoClient.getDatabase(database);
        DB.users = DB.database.getCollection(usersCollection);
    }
    
    /**
     * Logs the user in
     *
     * @param login The login credentials of the user
     * @return True if the login was successful
     */
    public static boolean login(LoginMessage login) {
        String salt = getSalt(login.getUsernameOrEmail().toLowerCase());
        if (salt.isEmpty()) {
            return false;
        }
        hashPasswordOnLogin(login, salt);
        return users
                .find(
                        or(
                                and(
                                        eq(Values.USERS_COLLECTION_USERNAME, login.getUsernameOrEmail().toLowerCase()),
                                        eq(Values.USERS_COLLECTION_PASSWORD, login.getPassword())
                                ),
                                and(
                                        eq(Values.USERS_COLLECTION_EMAIL, login.getUsernameOrEmail().toLowerCase()),
                                        eq(Values.USERS_COLLECTION_PASSWORD, login.getPassword())
                                )
                        )
                )
                .first() != null;
    }
    
    private static void hashPasswordOnLogin(LoginMessage message, String salt) {
        String saltedPassword = BCrypt.hashpw(message.getPassword(), salt);
        message.setPassword(saltedPassword);
    }
    
    /**
     * Gets the salt for a user
     *
     * @param usernameOrEmail The username or email of the user
     * @return The salt of the user or an empty String if the user doesn't exist or does not have a salt
     */
    private static String getSalt(String usernameOrEmail) {
        Document first = users
                .find(
                        or(
                                eq(Values.USERS_COLLECTION_USERNAME, usernameOrEmail.toLowerCase()),
                                eq(Values.USERS_COLLECTION_EMAIL, usernameOrEmail.toLowerCase())
                        )
                )
                .first();
        if (first == null) {
            return "";
        }
        return first.getOrDefault(Values.USERS_COLLECTION_SALT, "").toString();
    }
    
    /**
     * @param email The email to check for duplicates
     * @return True if this email already exists
     */
    public static synchronized boolean checkEmail(String email) {
        return users
                .find(
                        eq(Values.USERS_COLLECTION_EMAIL, email.toLowerCase())
                )
                .first() != null;
    }
    
    /**
     * @param username The username to check for duplicates
     * @return True if this username already exists
     */
    public static synchronized boolean checkUsername(String username) {
        return users
                .find(
                        eq(Values.USERS_COLLECTION_USERNAME, username.toLowerCase())
                )
                .first() != null;
    }
    
    /**
     * @param login The username and the password to check for duplicates
     * @return True ich this username or email already exists
     */
    public static synchronized boolean checkEmailAndUsername(LoginMessage login) {
        return users
                .find(
                        or(
                                eq(Values.USERS_COLLECTION_USERNAME, login.getUsernameOrEmail().toLowerCase()),
                                eq(Values.USERS_COLLECTION_EMAIL, login.getUsernameOrEmail().toLowerCase())
                        )
                )
                .first() != null;
    }
    
    /**
     * Registers a new user
     *
     * @param message The user registration data
     * @return true if the registration was successful
     */
    public static boolean register(RegisterMessage message) {
        String salt = BCrypt.gensalt();
        hashPasswordOnRegister(message, salt);
        
        Document doc = new Document();
        doc.put(Values.USERS_COLLECTION_USERNAME, message.getUsername());
        doc.put(Values.USERS_COLLECTION_EMAIL, message.getEmail());
        doc.put(Values.USERS_COLLECTION_PASSWORD, message.getPassword());
        doc.put(Values.USERS_COLLECTION_SALT, salt);
        
        try {
            users.insertOne(doc);
            return true;
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, e.getLocalizedMessage());
            return false;
        }
    }
    
    private static void hashPasswordOnRegister(RegisterMessage message, String salt) {
        String saltedPassword = BCrypt.hashpw(message.getPassword(), salt);
        message.setPassword(saltedPassword);
    }
    
}
