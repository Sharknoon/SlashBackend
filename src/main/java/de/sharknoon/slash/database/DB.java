package de.sharknoon.slash.database;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.login.LoginMessage;
import de.sharknoon.slash.networking.endpoints.register.RegisterMessage;
import de.sharknoon.slash.properties.*;
import de.sharknoon.slash.properties.Properties;
import org.bson.Document;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.*;

import static com.mongodb.client.model.Filters.*;

public class DB {
    
    private static MongoDatabase database;
    private static MongoCollection<Document> users;
    private static final Collation caseInsensitiveCollation = Collation.builder().locale("en").collationStrength(CollationStrength.SECONDARY).build();
    
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
     * @return The user if the login was successful
     */
    public static Optional<User> login(LoginMessage login) {
        String salt = getSalt(login.getUsernameOrEmail());
        if (salt.isEmpty()) {
            return Optional.empty();
        }
        hashPasswordOnLogin(login, salt);
        Document userDocument = users
                .find(
                        or(
                                and(
                                        eq(Values.USERS_COLLECTION_USERNAME, login.getUsernameOrEmail()),
                                        eq(Values.USERS_COLLECTION_PASSWORD, login.getPassword())
                                ),
                                and(
                                        eq(Values.USERS_COLLECTION_EMAIL, login.getUsernameOrEmail()),
                                        eq(Values.USERS_COLLECTION_PASSWORD, login.getPassword())
                                )
                        )
                )
                .collation(caseInsensitiveCollation)
                .first();
        if (userDocument == null) {
            return Optional.empty();
        }
        User user = new User(userDocument);
        return Optional.of(user);
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
                                eq(Values.USERS_COLLECTION_USERNAME, usernameOrEmail),
                                eq(Values.USERS_COLLECTION_EMAIL, usernameOrEmail)
                        )
                )
                .collation(caseInsensitiveCollation)
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
    public static synchronized boolean existsEmail(String email) {
        return users
                .find(
                        eq(Values.USERS_COLLECTION_EMAIL, email)
                )
                .collation(caseInsensitiveCollation)
                .first() != null;
    }
    
    /**
     * @param username The username to check for duplicates
     * @return True if this username already exists
     */
    public static synchronized boolean existsUsername(String username) {
        return users
                .find(
                        eq(Values.USERS_COLLECTION_USERNAME, username)
                )
                .collation(caseInsensitiveCollation)
                .first() != null;
    }
    
    /**
     * @param login The username and the password to check for duplicates
     * @return True ich this username or email already exists
     */
    public static synchronized boolean existsEmailOrUsername(LoginMessage login) {
        return users
                .find(
                        or(
                                eq(Values.USERS_COLLECTION_USERNAME, login.getUsernameOrEmail()),
                                eq(Values.USERS_COLLECTION_EMAIL, login.getUsernameOrEmail())
                        )
                )
                .collation(caseInsensitiveCollation)
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
        String registrationDate = LocalDateTime.now().toString();
        
        Document doc = new Document();
        doc.put(Values.USERS_COLLECTION_USERNAME, message.getUsername());
        doc.put(Values.USERS_COLLECTION_EMAIL, message.getEmail());
        doc.put(Values.USERS_COLLECTION_PASSWORD, message.getPassword());
        doc.put(Values.USERS_COLLECTION_SALT, salt);
        doc.put(Values.USERS_COLLECTION_REGISTRATION_DATE, registrationDate);
        
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
