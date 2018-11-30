package de.sharknoon.slash.database;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.PushOptions;
import de.sharknoon.slash.database.models.Chat;
import de.sharknoon.slash.database.models.Project;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.database.models.message.Message;
import de.sharknoon.slash.networking.utils.JavaURLCodec;
import de.sharknoon.slash.properties.DBConfig;
import de.sharknoon.slash.properties.Properties;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static com.mongodb.client.model.CollationStrength.SECONDARY;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Updates.*;
import static de.sharknoon.slash.database.Values.*;
import static org.bson.codecs.configuration.CodecRegistries.*;

public class DB {
    
    //Collations for indexes
    private static final Collation caseInsensitiveCollation = Collation.builder().locale("en").collationStrength(SECONDARY).build();
    //PushOptions
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
                    fromCodecs(new JavaURLCodec()),
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
    
    /**
     * USE WITH CAUTION NO WARRANTY FOR ANY DAMAGE, USE ONLY FOR TESTS
     *
     * @return the database instance
     */
    public static MongoDatabase leakDatabase() {
        return database;
    }
    
    
    //
    // LOGIN
    //
    
    /**
     * Gets a user
     *
     * @param usernameOrEmail The email or password of the user
     * @return The user if the getUserID was successful
     */
    public static Optional<User> getUserByUsernameOrEmail(String usernameOrEmail) {
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
    
    public static void addDeviceID(User user, String deviceID) {
        User u2 = users.find(
                and(
                        eq(COLLECTION_ID.value, user.id),
                        in(USERS_COLLECTION_DEVICE_IDS.value, deviceID)
                )
        ).first();
        //The device id is already added
        if (u2 != null) {
            return;
        }
        DB.users.updateOne(
                eq(COLLECTION_ID.value, user.id),
                pushEach(USERS_COLLECTION_DEVICE_IDS.value, List.of(deviceID), maxDevicesSlice)
        );
        user.deviceIDs.add(deviceID);
    }
    
    public static void removeDeviceID(User user, String deviceID) {
        users.updateOne(
                eq(COLLECTION_ID.value, user.id),
                pull(USERS_COLLECTION_DEVICE_IDS.value, deviceID)
        );
        user.deviceIDs.remove(deviceID);
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
     * @param usernameOrEmail The username/email to check for duplicates
     * @return True ich this username or email already exists
     */
    public static synchronized boolean existsEmailOrUsername(String usernameOrEmail) {
        return users
                .find(
                        or(
                                eq(USERS_COLLECTION_USERNAME.value, usernameOrEmail),
                                eq(USERS_COLLECTION_EMAIL.value, usernameOrEmail)
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
            Logger.getGlobal().log(Level.WARNING, "Could not register user", e);
            return false;
        }
    }
    
    /**
     * Unregisters a user, mainly used in tests for now
     *
     * @param user the user to be deleted
     */
    public static void unregister(User user) {
        users.deleteOne(eq(COLLECTION_ID.value, user.id));
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
            Logger.getGlobal().log(Level.WARNING, "Could not add project", e);
        }
    }
    
    public static Optional<Project> getProject(ObjectId projectID) {
        return Optional.ofNullable(
                projects
                        .find(eq(COLLECTION_ID.value, projectID))
                        .first()
        );
    }
    
    public static void addMessageToProject(Project project, Message message) {
        projects.updateOne(
                eq(COLLECTION_ID.value, project.id),
                pushEach(PROJECTS_COLLECTION_MESSAGES.value, List.of(message), maxStoredMessagesSlice)
        );
        project.messages.add(message);
    }
    
    public static void addUserToProject(Project project, User user) {
        projects.updateOne(
                eq(COLLECTION_ID.value, project.id),
                addToSet(PROJECTS_COLLECTION_USERS.value, user.id)
        );
        project.users.add(user.id);
    }
    
    public static void removeUserFromProject(Project project, User user) {
        projects.updateOne(
                eq(COLLECTION_ID.value, project.id),
                pull(PROJECTS_COLLECTION_USERS.value, user.id)
        );
        project.users.remove(user.id);
    }
    
    //
    // CHATS
    //
    
    public static Set<Chat> getNLastChatsForUser(ObjectId id, int n) {
        return chats
                .find(
                        or(
                                eq(CHATS_COLLECTION_PERSON_A.value, id),
                                eq(CHATS_COLLECTION_PERSON_B.value, id)
                        )
                )
                .sort(descending(CHATS_COLLECTION_MESSAGES.value + "." + CHATS_COLLECTION_MESSAGES_CREATION_DATE.value))
                .limit(n)
                .into(new HashSet<>());
    }
    
    public static Optional<Chat> getChatByPartnerID(ObjectId userID, ObjectId partnerID) {
        return Optional.ofNullable(
                chats
                        .find(
                                or(
                                        and(
                                                eq(CHATS_COLLECTION_PERSON_A.value, userID),
                                                eq(CHATS_COLLECTION_PERSON_B.value, partnerID)
                                        ),
                                        and(
                                                eq(CHATS_COLLECTION_PERSON_A.value, partnerID),
                                                eq(CHATS_COLLECTION_PERSON_B.value, userID)
                                        )
                                )
                        ).first()
        );
    }
    
    public static void addChat(Chat chat) {
        try {
            chats.insertOne(chat);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "Could not add chat", e);
        }
    }
    
    
    public static Optional<Chat> getChat(ObjectId objectId) {
        return Optional.ofNullable(
                chats.find(
                        eq(COLLECTION_ID.value, objectId)
                ).first()
        );
    }
    
    public static void addMessageToChat(Chat chat, Message message) {
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
    
    public static Set<User> searchUsers(String search) {
        if (search == null || search.isEmpty()) {
            return Set.of();
        }
        return users
                .find(regex(USERS_COLLECTION_USERNAME.value, ".*" + Pattern.quote(search) + ".*", "i"))
                .limit(10)
                .into(new HashSet<>());
    }
    
    public static Optional<User> getUserBySessionID(String sessionID) {
        return Optional.ofNullable(
                users
                        .find(in(USERS_COLLECTION_SESSION_IDS.value, sessionID))
                        .first()
        );
    }
}
