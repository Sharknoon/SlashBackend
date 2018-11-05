package de.sharknoon.slash.database;

import com.mongodb.*;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import de.sharknoon.slash.database.models.*;
import de.sharknoon.slash.networking.endpoints.login.LoginMessage;
import de.sharknoon.slash.properties.*;
import de.sharknoon.slash.properties.Properties;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

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
    private static final PushOptions maxStoredMessagesSlice = new PushOptions().slice(-Properties.getUserConfig().amountstoredchatmessages());
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
                    fromProviders(PojoCodecProvider
                            .builder()
                            .automatic(true)
                            .build()
                    )
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
     * @param usernameOrEmail The login credentials of the user
     * @return The user if the login was successful
     */
    public static Optional<User> login(String usernameOrEmail) {
        User user = users
                .find(
                        or(
                                eq(USERS_COLLECTION_USERNAME.value, usernameOrEmail),
                                eq(USERS_COLLECTION_EMAIL.value, usernameOrEmail)
                        )
                )
                .collation(caseInsensitiveCollation)
                .first();
        return Optional.ofNullable(user);
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
     * @param user The user registration data
     * @return true if the registration was successful
     */
    public static boolean register(User user) {
        try {
            users.insertOne(user);
            return true;
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, e.getLocalizedMessage());
            return false;
        }
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
    
    public static void addProject(Project project) {
        try {
            projects.insertOne(project);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, e.getLocalizedMessage());
        }
    }
    
    public static Optional<Project> getProject(ObjectId projectID) {
        return Optional.ofNullable(
                projects
                        .find(eq(COLLECTION_ID.value, projectID))
                        .first()
        );
    }
    
    //Not yet needed
//    public static boolean existsProjectID(ObjectId id) {
//        return projects
//                .find(eq(COLLECTION_ID.value, id))
//                .first() != null;
//    }
    
    //
    // CHATS
    //
    
    public static Set<Chat> getNLastChatsForUser(ObjectId id, int n) {
        return chats
                .find(
                        eq(CHATS_COLLECTION_PERSON_A.value, id)
                )
                .sort(descending(CHATS_COLLECTION_CREATION_DATE.value))
                .limit(n)
                .into(new HashSet<>());
    }
    
    public static Optional<Chat> getChatByPartnerID(ObjectId id) {
        return Optional.ofNullable(
                chats
                        .find(
                                eq(CHATS_COLLECTION_PERSON_B.value, id)
                        ).first()
        );
    }
    
    public static void addChat(Chat chat) {
        try {
            chats.insertOne(chat);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, e.getLocalizedMessage());
        }
    }
    
    
    public static Optional<Chat> getChat(ObjectId objectId) {
        return Optional.ofNullable(
                chats.find(
                        eq(COLLECTION_ID.value, objectId)
                ).first()
        );
    }
    
    public static void addMessageToChat(Chat chat, String message) {
        chats.updateOne(
                eq(COLLECTION_ID.value, chat.id),
                pushEach(CHATS_COLLECTION_MESSAGES.value, List.of(message), maxStoredMessagesSlice)
        );
        chat.messages.add(message);
    }
    
    //
    // USER
    //
    
    public static Optional<User> getUser(ObjectId id) {
        return Optional.ofNullable(
                users
                        .find(eq(COLLECTION_ID.value, id))
                        .first()
        );
    }
    
    public static Optional<User> getUserForUsername(String username) {
        return Optional.ofNullable(
                users
                        .find(eq(USERS_COLLECTION_USERNAME.value, username))
                        .collation(caseInsensitiveCollation)
                        .first()
        );
    }
    
}
