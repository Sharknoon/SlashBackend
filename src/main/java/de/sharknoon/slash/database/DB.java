package de.sharknoon.slash.database;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import de.sharknoon.slash.database.models.*;
import de.sharknoon.slash.networking.endpoints.login.LoginMessage;
import de.sharknoon.slash.networking.endpoints.register.RegisterMessage;
import de.sharknoon.slash.properties.*;
import de.sharknoon.slash.properties.Properties;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.*;

import static com.mongodb.client.model.CollationStrength.SECONDARY;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.*;
import static de.sharknoon.slash.database.Values.*;
import static org.bson.codecs.configuration.CodecRegistries.*;

public class DB {
    
    //Collations for indexes
    private static final Collation caseInsensitiveCollation = Collation.builder().locale("en").collationStrength(SECONDARY).build();
    //Pushoptions
    private static final PushOptions maxDevicesSlice = new PushOptions().slice(-Properties.getUserConfig().maxdevices());
    //The database
    private static MongoDatabase database;
    //The collections
    private static MongoCollection<User> users;
    private static MongoCollection<Project> projects;
    private static MongoCollection<Chat> chats;
    
    static {
        try {
            DBConfig props = Properties.getDBConfig();
        
            String ip = props.databaseip();
            int port = props.databaseport();
            String database = props.database();
            String username = props.dbuser();
            String password = props.dbpassword();
        
            MongoCredential credential = MongoCredential.createCredential(username, database, password.toCharArray());
            CodecRegistry codecRegistry = fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    fromProviders(PojoCodecProvider.builder().automatic(true).build())
            );
            MongoClient mongoClient = MongoClients.create(
                    MongoClientSettings
                            .builder()
                            .applyToClusterSettings(builder ->
                                    builder.hosts(List.of(new ServerAddress(ip, port)))
                            )
                            .credential(credential)
                            .codecRegistry(codecRegistry)
                            .build()
            );
        
        
            DB.database = mongoClient.getDatabase(database);
            DB.users = DB.database.getCollection(USERS_COLLECTION.value, User.class);
            DB.projects = DB.database.getCollection(PROJECTS_COLLECTION.value, Project.class);
            DB.chats = DB.database.getCollection(CHATS_COLLECTION.value, Chat.class);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Database not reachable", e);
            System.exit(1);
        }
    }
    
    //
    // LOGIN
    //
    
    /**
     * Logs the user in
     *
     * @param login The login credentials of the user
     * @return The user if the login was successful
     */
    public static Optional<User> login(LoginMessage login) {
        User user = users
                .find(
                        or(
                                eq(USERS_COLLECTION_USERNAME.value, login.getUsernameOrEmail()),
                                eq(USERS_COLLECTION_EMAIL.value, login.getUsernameOrEmail())
                        )
                )
                .collation(caseInsensitiveCollation)
                .first();
        if (user == null) {
            return Optional.empty();
        }
        String salt = user.salt;
        hashPasswordOnLogin(login, salt);
        if (login.getPassword().equals(user.password)) {
            return Optional.of(user);
        }
        return Optional.empty();
    }
    
    public static void addSessionID(User user, String sessionID) {
        users.updateOne(
                eq(COLLECTION_ID.value, user.id),
                pushEach(USERS_COLLECTION_SESSION_IDS.value, List.of(sessionID), maxDevicesSlice)
        );
        user.sessionIDs.add(sessionID);
    }
    
    public static void removeSessionID(User user, String sessionID) {
        users.updateOne(
                eq(COLLECTION_ID.value, user.id),
                pull(USERS_COLLECTION_SESSION_IDS.value, sessionID)
        );
        user.sessionIDs.remove(sessionID);
    }
    
    private static void hashPasswordOnLogin(LoginMessage message, String salt) {
        String saltedPassword = BCrypt.hashpw(message.getPassword(), salt);
        message.setPassword(saltedPassword);
    }
    
    //
    // REGISTER
    //
    
    /**
     * @param email The email to check for duplicates
     * @return True if this email already exists
     */
    public static synchronized boolean existsEmail(String email) {
        return users
                .find(
                        eq(USERS_COLLECTION_EMAIL.value, email)
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
                        eq(USERS_COLLECTION_USERNAME.value, username)
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
                                eq(USERS_COLLECTION_USERNAME.value, login.getUsernameOrEmail()),
                                eq(USERS_COLLECTION_EMAIL.value, login.getUsernameOrEmail())
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
        LocalDateTime registrationDate = LocalDateTime.now();
    
        User user = new User();
        user.username = message.getUsername();
        user.email = message.getEmail();
        user.password = message.getPassword();
        user.salt = salt;
        user.registrationDate = registrationDate;
        user.sessionIDs = Set.of();
        
        try {
            users.insertOne(user);
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
    
    //
    // PROJECTS
    //
    
    public static Set<Project> getProjectsForUser(User u) {
        ObjectId userId = u.id;
        return projects
                .find(
                        in(PROJECTS_COLLECTION_USERS.value, userId)
                )
                .into(new HashSet<>());
    }
    
    //
    // CHATS
    //
    
    public static Set<Chat> getNLastChatsForUser(User u, int n) {
        ObjectId userId = u.id;
        return chats
                .find(
                        or(
                                eq(CHATS_COLLECTION_PERSON_A.value, userId),
                                eq(CHATS_COLLECTION_PERSON_B.value, userId)
                        )
                )
                .sort(descending(CHATS_COLLECTION_CREATION_DATE.value))
                .limit(n)
                .into(new HashSet<>());
    }
    
}
