package de.sharknoon.slash.networking.endpoints.home;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.Login;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.database.models.message.Message;
import de.sharknoon.slash.database.models.message.MessageEmotion;
import de.sharknoon.slash.database.models.message.MessageType;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.endpoints.TestSession;
import de.sharknoon.slash.networking.endpoints.home.handlers.response.*;
import de.sharknoon.slash.networking.endpoints.home.messages.*;
import de.sharknoon.slash.networking.endpoints.login.LoginEndpoint;
import de.sharknoon.slash.networking.endpoints.login.LoginMessage;
import de.sharknoon.slash.serialisation.gsonconverter.LocalDateTimeConverter;
import de.sharknoon.slash.serialisation.gsonconverter.ObjectIdConverter;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.websocket.Session;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

class HomeEndpointTest {
    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeConverter())
            .registerTypeAdapter(ObjectId.class, new ObjectIdConverter())
            .excludeFieldsWithModifiers(Modifier.STATIC)
            .create();

    private static final User user1 = new User();
    private static final User user2 = new User();
    private static String sendText = "";
    private static Session s = new TestSession(t -> sendText = t);
    private static final List<ObjectId> USER_IDS_TO_DELETE = new ArrayList<>();
    private static final List<ObjectId> PROJECT_IDS_TO_DELETE = new ArrayList<>();
    private static final List<ObjectId> CHAT_IDS_TO_DELETE = new ArrayList<>();

    private static void registerUsers() {
        user1.id = new ObjectId();
        user1.username = UUID.randomUUID().toString().substring(0, 15);
        user1.ids = new HashSet<>();
        user1.salt = BCrypt.gensalt();
        user1.registrationDate = LocalDateTime.now().withNano(0);
        user1.password = BCrypt.hashpw("123456", user1.salt);
        user1.email = user1.username + "@web.de";
        USER_IDS_TO_DELETE.add(user1.id);

        DB.register(user1);

        user2.id = new ObjectId();
        user2.username = UUID.randomUUID().toString().substring(0, 15);
        user2.ids = new HashSet<>();
        user2.salt = BCrypt.gensalt();
        user2.registrationDate = LocalDateTime.now().withNano(0);
        user2.password = BCrypt.hashpw("123456", user1.salt);
        user2.email = user2.username + "@gmail.com";
        USER_IDS_TO_DELETE.add(user2.id);

        DB.register(user2);
    }

    private static void loginUsers() {
        LoginEndpoint le = new LoginEndpoint();
        le.onOpen(s);

        LoginMessage lm = new LoginMessage();
        lm.setUsernameOrEmail(user1.email);
        lm.setPassword("123456");
        lm.setDeviceID("123456789");
        le.onTextMessage(s, lm);
        LoginAnswer la = new Gson().fromJson(sendText, LoginAnswer.class);
        Login l = new Login();
        l.deviceID = lm.getDeviceID();
        l.sessionID = la.sessionid;
        user1.ids.add(l);

        lm.setUsernameOrEmail(user2.email);
        lm.setPassword("123456");
        lm.setDeviceID("987654321");
        le.onTextMessage(s, lm);
        la = new Gson().fromJson(sendText, LoginAnswer.class);
        Login l2 = new Login();
        l2.deviceID = lm.getDeviceID();
        l2.sessionID = la.sessionid;
        user2.ids.add(l2);
    }

    @BeforeAll
    static void setUp() {
        registerUsers();
        loginUsers();
    }

    @AfterAll
    static void tearDown() {
        USER_IDS_TO_DELETE.forEach(u ->
                DB.leakDatabase().getCollection("users").deleteOne(eq("_id", u))
        );
        CHAT_IDS_TO_DELETE.forEach(u ->
                DB.leakDatabase().getCollection("chats").deleteOne(eq("_id", u))
        );
        PROJECT_IDS_TO_DELETE.forEach(u ->
                DB.leakDatabase().getCollection("projects").deleteOne(eq("_id", u))
        );
    }


    @Test
    void test_getHomeStatus() {
        HomeEndpoint he = new HomeEndpoint();

        StatusAndSessionIDMessage sasm = new StatusAndSessionIDMessage(Status.GET_HOME);
        sasm.setSessionid(UUID.randomUUID().toString());
        he.onOpen(s);
        he.onTextMessage(s, gson.toJson(sasm));

        // Wrong session id
        Assertions.assertEquals("{\"status\":\"NO_LOGIN_OR_TOO_MUCH_DEVICES\",\"messageType\":\"You are either not logged in or using more than 5 devices\"}", sendText);

        // Correct session id
        sasm.setSessionid(user1.ids.iterator().next().sessionID);
        he.onTextMessage(s, gson.toJson(sasm));
        Assertions.assertEquals("{\"status\":\"OK_HOME\",\"projects\":[],\"chats\":[]}", sendText);
    }

    @Test
    void test_getUsersStatus() {
        HomeEndpoint he = new HomeEndpoint();

        GetUsersMessage getUsersMessage = new GetUsersMessage();
        getUsersMessage.setSessionid(user1.ids.iterator().next().sessionID);

        he.onOpen(s);
        he.onTextMessage(s, gson.toJson(getUsersMessage));
        Assertions.assertEquals("{\"status\":\"OK_USERS\",\"users\":[]}", sendText);

        getUsersMessage.setSearch(user1.username.substring(2, 13));
        he.onTextMessage(s, gson.toJson(getUsersMessage));

        UsersResponse ur = gson.fromJson(sendText, UsersResponse.class);
        Assertions.assertTrue(ur.users.stream().anyMatch(u -> u.id.equals(user1.id)));
        Assertions.assertTrue(ur.users.stream().anyMatch(u -> u.username.equals(user1.username)));

        getUsersMessage.setSearch(user2.username.toUpperCase());
        he.onTextMessage(s, gson.toJson(getUsersMessage));

        ur = gson.fromJson(sendText, UsersResponse.class);
        Assertions.assertTrue(ur.users.stream().anyMatch(u -> u.id.equals(user2.id)));
        Assertions.assertTrue(ur.users.stream().anyMatch(u -> u.username.equals(user2.username)));
    }

    @Test
    void test_getChatStatus() {
        HomeEndpoint he = new HomeEndpoint();

        GetChatMessage getChatMessage = new GetChatMessage();
        getChatMessage.setSessionid(UUID.randomUUID().toString());
        getChatMessage.setPartnerUserID("blargh");
        he.onOpen(s);

        he.onTextMessage(s, gson.toJson(getChatMessage));
        Assertions.assertEquals("{\"status\":\"NO_LOGIN_OR_TOO_MUCH_DEVICES\",\"messageType\":\"You are either not logged in or using more than 5 devices\"}", sendText);

        getChatMessage.setSessionid(user1.ids.iterator().next().sessionID);
        he.onTextMessage(s, gson.toJson(getChatMessage));
        Assertions.assertEquals("{\"status\":\"WRONG_USER_ID\",\"description\":\"The specified userID doesn\\u0027t conform to the right syntax\"}", sendText);

        getChatMessage.setPartnerUserID("5be0abfd0ab4e53cc82294f1");
        he.onTextMessage(s, gson.toJson(getChatMessage));
        Assertions.assertEquals("{\"status\":\"NO_USER_FOUND\",\"description\":\"No user with the specified id was found\"}", sendText);

        getChatMessage.setPartnerUserID(user2.id.toString());
        he.onTextMessage(s, gson.toJson(getChatMessage));

        ChatResponse cr = gson.fromJson(sendText, ChatResponse.class);
        CHAT_IDS_TO_DELETE.add(cr.chat.id);
        Assertions.assertEquals(user1.id, cr.chat.personA);
        Assertions.assertEquals(user2.id, cr.chat.personB);
        Assertions.assertEquals(user2.username, cr.chat.partnerUsername);

        DB.leakDatabase().getCollection("chats").deleteOne(eq("_id", cr.chat.id));
    }

    @Test
    void test_addProjectStatus() {
        HomeEndpoint he = new HomeEndpoint();

        AddProjectMessage addProjectMessage = new AddProjectMessage();
        addProjectMessage.setSessionid(user1.ids.iterator().next().sessionID);
        addProjectMessage.setProjectName("");
        he.onOpen(s);
        he.onTextMessage(s, gson.toJson(addProjectMessage));
        // Empty project name
        Assertions.assertEquals("{\"status\":\"WRONG_PROJECT_NAME\",\"description\":\"The project name doesn\\u0027t match the specifications\"}", sendText);

        String projectName = UUID.randomUUID().toString().substring(0, 15);
        addProjectMessage.setProjectName(projectName);
        String projectDescription = UUID.randomUUID().toString().substring(0, 15);
        addProjectMessage.setProjectDescription(projectDescription);
        addProjectMessage.setProjectOwner("asölodfkgjh");
        he.onTextMessage(s, gson.toJson(addProjectMessage));
        Assertions.assertEquals("{\"status\":\"WRONG_PROJECT_OWNER\",\"description\":\"The project owner is not a valid id\"}", sendText);

        String projectOwner = user2.id.toString();
        addProjectMessage.setProjectOwner(projectOwner);
        he.onTextMessage(s, gson.toJson(addProjectMessage));
        Assertions.assertTrue(sendText.startsWith("{\"status\":\"OK_PROJECT\",\"project\":"));
        final String projectStatus = sendText;

        ProjectResponse response = gson.fromJson(sendText, ProjectResponse.class);
        Assertions.assertEquals(response.project.projectOwner, user2.id);
        PROJECT_IDS_TO_DELETE.add(response.project.id);
        GetProjectMessage getProjectMessage = new GetProjectMessage();
        getProjectMessage.setProjectID(response.project.id.toString());
        getProjectMessage.setSessionid(user1.ids.iterator().next().sessionID);
        he.onTextMessage(s, gson.toJson(getProjectMessage));

        Assertions.assertEquals(projectStatus, sendText);

        DB.leakDatabase().getCollection("projects").deleteMany(eq("name", projectName));
    }

    @Test
    void test_getProjectStatus() {
        HomeEndpoint he = new HomeEndpoint();

        GetProjectMessage getProjectMessage = new GetProjectMessage();
        getProjectMessage.setSessionid(user1.ids.iterator().next().sessionID);
        getProjectMessage.setProjectID(StringUtils.EMPTY);
        he.onOpen(s);
        he.onTextMessage(s, gson.toJson(getProjectMessage));

        Assertions.assertEquals("{\"status\":\"WRONG_PROJECT_ID\",\"description\":\"The specified projectID doesn\\u0027t conform to the right syntax\"}", sendText);

        getProjectMessage.setProjectID("5be0b0dd0fb4e53cc82294f3");
        he.onTextMessage(s, gson.toJson(getProjectMessage));
        Assertions.assertEquals("{\"status\":\"NO_PROJECT_FOUND\",\"description\":\"No project with the specified id was found\"}", sendText);
    }

    @Test
    void test_addChatMessageStatus() {
        HomeEndpoint he = new HomeEndpoint();

        AddChatMessageMessage addChatMessageMessage = new AddChatMessageMessage();
        addChatMessageMessage.setSessionid(user1.ids.iterator().next().sessionID);
        he.onOpen(s);

        he.onTextMessage(s, gson.toJson(addChatMessageMessage));
        Assertions.assertEquals("{\"status\":\"WRONG_CHAT_ID\",\"description\":\"The syntax of the chat-ID was not correct\"}", sendText);

        addChatMessageMessage.setChatID("5be0a9d584b063292ffb66ce");
        he.onTextMessage(s, gson.toJson(addChatMessageMessage));
        Assertions.assertEquals("{\"status\":\"NO_CHAT_FOUND\",\"description\":\"No corresponding chat to the chat-ID was found\"}", sendText);

        //constructing a new chat between user1 and user2
        GetChatMessage getChatMessage = new GetChatMessage();
        getChatMessage.setPartnerUserID(user2.id.toString());
        getChatMessage.setSessionid(user1.ids.iterator().next().sessionID);
        he.onTextMessage(s, gson.toJson(getChatMessage));
        ChatResponse cr = gson.fromJson(sendText, ChatResponse.class);
        CHAT_IDS_TO_DELETE.add(cr.chat.id);

        addChatMessageMessage.setChatID(cr.chat.id.toString());
        he.onTextMessage(s, gson.toJson(addChatMessageMessage));
        Assertions.assertEquals("{\"status\":\"CHAT_MESSAGE_TYPE_INVALID\",\"description\":\"The chat message type doesn\\u0027t match the specification\"}", sendText);

        addChatMessageMessage.setMessageType(MessageType.TEXT);
        he.onTextMessage(s, gson.toJson(addChatMessageMessage));
        Assertions.assertEquals("{\"status\":\"CHAT_MESSAGE_CONTENT_TOO_LONG\",\"description\":\"The chat message content was over 5000 characters long\"}", sendText);

        addChatMessageMessage.setMessageContent("Hallo 123");
        he.onTextMessage(s, gson.toJson(addChatMessageMessage));

        ObjectId oldChatID = cr.chat.id;
        cr = gson.fromJson(sendText, ChatResponse.class);
        Assertions.assertEquals(oldChatID, cr.chat.id);
        Assertions.assertEquals(user1.id, cr.chat.personA);
        Assertions.assertEquals(user2.id, cr.chat.personB);
        Assertions.assertEquals(user2.username, cr.chat.partnerUsername);
        Message newMessage = cr.chat.messages.iterator().next();
        Assertions.assertEquals(user1.id, newMessage.sender);
        Assertions.assertEquals(MessageType.TEXT, newMessage.type);
        Assertions.assertEquals("Hallo 123", newMessage.content);

        addChatMessageMessage.setMessageType(MessageType.EMOTION);
        he.onTextMessage(s, gson.toJson(addChatMessageMessage));
        Assertions.assertEquals("{\"status\":\"CHAT_MESSAGE_SUBJECT_TOO_LONG\",\"description\":\"The chat message subject was too long\"}", sendText);

        addChatMessageMessage.setMessageSubject("Huhu 123");
        he.onTextMessage(s, gson.toJson(addChatMessageMessage));
        Assertions.assertEquals("{\"status\":\"CHAT_MESSAGE_EMOTION_NOT_SET\",\"description\":\"The chat message emotion was not set\"}", sendText);

        addChatMessageMessage.setMessageEmotion(MessageEmotion.CRITICISM);
        he.onTextMessage(s, gson.toJson(addChatMessageMessage));

        cr = gson.fromJson(sendText, ChatResponse.class);
        Assertions.assertEquals(oldChatID, cr.chat.id);
        Assertions.assertEquals(user1.id, cr.chat.personA);
        Assertions.assertEquals(user2.id, cr.chat.personB);
        Assertions.assertEquals(user2.username, cr.chat.partnerUsername);
        Set<Message> messages = cr.chat.messages;
        Assertions.assertTrue(messages.stream().allMatch(m -> m.sender.equals(user1.id)));
        Assertions.assertTrue(messages.stream().anyMatch(m -> m.type == MessageType.EMOTION));
        Assertions.assertTrue(messages.stream().anyMatch(m -> m.type == MessageType.TEXT));
        Assertions.assertTrue(messages.stream().allMatch(m -> m.content.equals("Hallo 123")));
        Assertions.assertTrue(messages.stream().anyMatch(m -> Objects.equals(m.subject, "Huhu 123")));
        Assertions.assertTrue(messages.stream().anyMatch(m -> m.emotion == MessageEmotion.CRITICISM));

        DB.leakDatabase().getCollection("chats").deleteOne(eq("_id", cr.chat.id));
    }

    @Test
    void test_addProjectMessageStatus() {
        HomeEndpoint he = new HomeEndpoint();

        AddProjectMessageMessage addProjectMessageMessage = new AddProjectMessageMessage();
        addProjectMessageMessage.setSessionid(user1.ids.iterator().next().sessionID);
        he.onOpen(s);

        he.onTextMessage(s, gson.toJson(addProjectMessageMessage));
        Assertions.assertEquals("{\"status\":\"WRONG_PROJECT_ID\",\"description\":\"The syntax of the project-ID was not correct\"}", sendText);

        addProjectMessageMessage.setProjectID("5be0a9d584b063292ffb66ce");
        he.onTextMessage(s, gson.toJson(addProjectMessageMessage));
        Assertions.assertEquals("{\"status\":\"NO_PROJECT_FOUND\",\"description\":\"No corresponding project to the project-ID was found\"}", sendText);

        //constructing a new project
        AddProjectMessage addProjectMessage = new AddProjectMessage();
        addProjectMessage.setProjectName("Test123 Project");
        addProjectMessage.setProjectDescription("I am going to be deleted very soon");
        addProjectMessage.setSessionid(user1.ids.iterator().next().sessionID);
        he.onTextMessage(s, gson.toJson(addProjectMessage));
        ProjectResponse pr = gson.fromJson(sendText, ProjectResponse.class);
        PROJECT_IDS_TO_DELETE.add(pr.project.id);

        addProjectMessageMessage.setProjectID(pr.project.id.toString());
        he.onTextMessage(s, gson.toJson(addProjectMessageMessage));
        Assertions.assertEquals("{\"status\":\"PROJECT_MESSAGE_TYPE_INVALID\",\"description\":\"The project message type doesn\\u0027t match the specification\"}", sendText);

        addProjectMessageMessage.setMessageType(MessageType.TEXT);
        he.onTextMessage(s, gson.toJson(addProjectMessageMessage));
        Assertions.assertEquals("{\"status\":\"PROJECT_MESSAGE_CONTENT_TOO_LONG\",\"description\":\"The project message content was over 5000 characters long\"}", sendText);

        addProjectMessageMessage.setMessageContent("Hallo 123");
        he.onTextMessage(s, gson.toJson(addProjectMessageMessage));

        ObjectId oldProjectID = pr.project.id;
        pr = gson.fromJson(sendText, ProjectResponse.class);
        Assertions.assertEquals(oldProjectID, pr.project.id);
        Assertions.assertEquals("Test123 Project", pr.project.name);
        Assertions.assertEquals("I am going to be deleted very soon", pr.project.description);
        Message newMessage = pr.project.messages.iterator().next();
        Assertions.assertEquals(user1.id, newMessage.sender);
        Assertions.assertEquals(MessageType.TEXT, newMessage.type);
        Assertions.assertEquals("Hallo 123", newMessage.content);

        addProjectMessageMessage.setMessageType(MessageType.EMOTION);
        he.onTextMessage(s, gson.toJson(addProjectMessageMessage));
        Assertions.assertEquals("{\"status\":\"PROJECT_MESSAGE_SUBJECT_TOO_LONG\",\"description\":\"The project message subject was too long\"}", sendText);

        addProjectMessageMessage.setMessageSubject("Huhu 123");
        he.onTextMessage(s, gson.toJson(addProjectMessageMessage));
        Assertions.assertEquals("{\"status\":\"PROJECT_MESSAGE_EMOTION_NOT_SET\",\"description\":\"The project message emotion was not set\"}", sendText);

        addProjectMessageMessage.setMessageEmotion(MessageEmotion.CRITICISM);
        he.onTextMessage(s, gson.toJson(addProjectMessageMessage));

        pr = gson.fromJson(sendText, ProjectResponse.class);
        Assertions.assertEquals(oldProjectID, pr.project.id);
        Assertions.assertEquals("Test123 Project", pr.project.name);
        Assertions.assertEquals("I am going to be deleted very soon", pr.project.description);
        Set<Message> messages = pr.project.messages;
        Assertions.assertTrue(messages.stream().allMatch(m -> m.sender.equals(user1.id)));
        Assertions.assertTrue(messages.stream().anyMatch(m -> m.type == MessageType.EMOTION));
        Assertions.assertTrue(messages.stream().anyMatch(m -> m.type == MessageType.TEXT));
        Assertions.assertTrue(messages.stream().allMatch(m -> m.content.equals("Hallo 123")));
        Assertions.assertTrue(messages.stream().anyMatch(m -> Objects.equals(m.subject, "Huhu 123")));
        Assertions.assertTrue(messages.stream().anyMatch(m -> m.emotion == MessageEmotion.CRITICISM));


        DB.leakDatabase().getCollection("projects").deleteOne(eq("_id", pr.project.id));
    }

    @Test
    void test_logoutStatus() {
        HomeEndpoint he = new HomeEndpoint();

        StatusAndSessionIDMessage statusAndSessionIDMessage = new StatusAndSessionIDMessage(Status.LOGOUT);
        statusAndSessionIDMessage.setSessionid(user1.ids.iterator().next().sessionID);

        he.onOpen(s);
        Assertions.assertTrue(s.isOpen());
        he.onTextMessage(s, gson.toJson(statusAndSessionIDMessage));
        Assertions.assertFalse(s.isOpen());

        s = new TestSession(t -> sendText = t);

        he.onTextMessage(s, gson.toJson(statusAndSessionIDMessage));
        Assertions.assertEquals("{\"status\":\"NO_LOGIN_OR_TOO_MUCH_DEVICES\",\"messageType\":\"You are either not logged in or using more than 5 devices\"}", sendText);

        //Log back in
        LoginEndpoint le = new LoginEndpoint();
        le.onOpen(s);

        LoginMessage lm = new LoginMessage();
        lm.setUsernameOrEmail(user1.email);
        lm.setPassword("123456");
        lm.setDeviceID("123456789");
        le.onTextMessage(s, lm);
        LoginAnswer la = new Gson().fromJson(sendText, LoginAnswer.class);
        user1.ids.clear();
        Login l = new Login();
        l.sessionID = la.sessionid;
        l.deviceID = lm.getDeviceID();
        user1.ids.add(l);
    }

    @Test
    void test_getUserStatus() {
        HomeEndpoint he = new HomeEndpoint();

        GetUserMessage getUserMessage = new GetUserMessage();
        getUserMessage.setSessionid(user1.ids.iterator().next().sessionID);

        he.onOpen(s);
        he.onTextMessage(s, gson.toJson(getUserMessage));
        Assertions.assertEquals("{\"status\":\"NO_USER_FOUND\",\"description\":\"No user with the specified id was found\"}", sendText);

        getUserMessage.setUserID(user1.id.toString());
        he.onTextMessage(s, gson.toJson(getUserMessage));

        UserResponse ur = gson.fromJson(sendText, UserResponse.class);
        Assertions.assertEquals(ur.user.id, user1.id);
        Assertions.assertEquals(ur.user.username, user1.username);

        getUserMessage.setUserID(user2.id.toString());
        he.onTextMessage(s, gson.toJson(getUserMessage));

        ur = gson.fromJson(sendText, UserResponse.class);
        Assertions.assertEquals(ur.user.id, user2.id);
        Assertions.assertEquals(ur.user.username, user2.username);
    }

    @Test
    void test_modifyProjectUsersStatus() {
        HomeEndpoint he = new HomeEndpoint();

        ModifyProjectUsersMessage modifyProjectUsersMessage = new ModifyProjectUsersMessage();
        modifyProjectUsersMessage.setSessionid(user1.ids.iterator().next().sessionID);

        he.onOpen(s);
        he.onTextMessage(s, gson.toJson(modifyProjectUsersMessage));
        Assertions.assertEquals("{\"status\":\"NO_USER_FOUND\",\"description\":\"No user with the specified id was found\"}", sendText);

        modifyProjectUsersMessage.setUserID(user2.id.toString());
        he.onTextMessage(s, gson.toJson(modifyProjectUsersMessage));
        Assertions.assertEquals("{\"status\":\"NO_PROJECT_FOUND\",\"description\":\"No project with the specified id was found\"}", sendText);

        //constructing a new project
        AddProjectMessage addProjectMessage = new AddProjectMessage();
        addProjectMessage.setProjectName("Test123 Project");
        addProjectMessage.setProjectDescription("I am going to be deleted very soon");
        addProjectMessage.setSessionid(user1.ids.iterator().next().sessionID);
        he.onTextMessage(s, gson.toJson(addProjectMessage));
        ProjectResponse pr = gson.fromJson(sendText, ProjectResponse.class);
        PROJECT_IDS_TO_DELETE.add(pr.project.id);

        sendText = "";
        modifyProjectUsersMessage.setProjectID(pr.project.id.toString());
        he.onTextMessage(s, gson.toJson(modifyProjectUsersMessage));
        Assertions.assertEquals("{\"status\":\"OK\"}", sendText);

        GetProjectMessage getProjectMessage = new GetProjectMessage();
        getProjectMessage.setSessionid(user1.ids.iterator().next().sessionID);
        getProjectMessage.setProjectID(pr.project.id.toString());
        he.onTextMessage(s, gson.toJson(getProjectMessage));
        ProjectResponse pr2 = gson.fromJson(sendText, ProjectResponse.class);
        Assertions.assertTrue(pr2.project.usernames.stream().map(u -> u.id).anyMatch(o -> o.equals(user2.id)));

        modifyProjectUsersMessage.setAddUser(false);
        he.onTextMessage(s, gson.toJson(modifyProjectUsersMessage));
        he.onTextMessage(s, gson.toJson(getProjectMessage));
        pr2 = gson.fromJson(sendText, ProjectResponse.class);
        Assertions.assertFalse(pr2.project.usernames.stream().anyMatch(o -> o.id.equals(user2.id)));

        DB.leakDatabase().getCollection("projects").deleteOne(eq("_id", pr.project.id));
    }

    @Test
    void test_modifyProjectOwnerStatus() {
        HomeEndpoint he = new HomeEndpoint();

        ModifyProjectOwnerMessage modifyProjectOwnerMessage = new ModifyProjectOwnerMessage();
        modifyProjectOwnerMessage.setSessionid(user1.ids.iterator().next().sessionID);

        he.onOpen(s);
        he.onTextMessage(s, gson.toJson(modifyProjectOwnerMessage));
        Assertions.assertEquals("{\"status\":\"NO_PROJECT_FOUND\",\"description\":\"No project with the specified id was found\"}", sendText);

        modifyProjectOwnerMessage.setProjectOwner(user2.id.toString());
        he.onTextMessage(s, gson.toJson(modifyProjectOwnerMessage));
        Assertions.assertEquals("{\"status\":\"NO_PROJECT_FOUND\",\"description\":\"No project with the specified id was found\"}", sendText);

        //constructing a new project
        AddProjectMessage addProjectMessage = new AddProjectMessage();
        addProjectMessage.setProjectName("Test123 Project");
        addProjectMessage.setProjectDescription("I am going to be deleted very soon");
        addProjectMessage.setSessionid(user1.ids.iterator().next().sessionID);
        he.onTextMessage(s, gson.toJson(addProjectMessage));
        ProjectResponse pr = gson.fromJson(sendText, ProjectResponse.class);
        PROJECT_IDS_TO_DELETE.add(pr.project.id);

        sendText = "";
        modifyProjectOwnerMessage.setProjectID(pr.project.id.toString());
        he.onTextMessage(s, gson.toJson(modifyProjectOwnerMessage));
        Assertions.assertEquals("{\"status\":\"OK\"}", sendText);

        GetProjectMessage getProjectMessage = new GetProjectMessage();
        getProjectMessage.setSessionid(user1.ids.iterator().next().sessionID);
        getProjectMessage.setProjectID(pr.project.id.toString());
        he.onTextMessage(s, gson.toJson(getProjectMessage));
        ProjectResponse pr2 = gson.fromJson(sendText, ProjectResponse.class);
        Assertions.assertEquals(pr2.project.projectOwner, user2.id);

        modifyProjectOwnerMessage.setProjectOwner("");
        he.onTextMessage(s, gson.toJson(modifyProjectOwnerMessage));
        he.onTextMessage(s, gson.toJson(getProjectMessage));
        pr2 = gson.fromJson(sendText, ProjectResponse.class);
        Assertions.assertNull(pr2.project.projectOwner);

        DB.leakDatabase().getCollection("projects").deleteOne(eq("_id", pr.project.id));
    }

    @Test
    void test_modifyProjectImageStatus() {
        HomeEndpoint he = new HomeEndpoint();

        ModifyProjectImageMessage modifyProjectImageMessage = new ModifyProjectImageMessage();
        modifyProjectImageMessage.setSessionid(user1.ids.iterator().next().sessionID);
        //modifyProjectImageMessage.setRemoved(false);

        he.onOpen(s);
        he.onTextMessage(s, gson.toJson(modifyProjectImageMessage));
        Assertions.assertEquals("{\"status\":\"NO_PROJECT_FOUND\",\"description\":\"No project with the specified id was found\"}", sendText);

        //constructing a new project
        AddProjectMessage addProjectMessage = new AddProjectMessage();
        addProjectMessage.setProjectName("Test123 Project");
        addProjectMessage.setProjectDescription("I am going to be deleted very soon");
        addProjectMessage.setSessionid(user1.ids.iterator().next().sessionID);
        he.onTextMessage(s, gson.toJson(addProjectMessage));
        ProjectResponse pr = gson.fromJson(sendText, ProjectResponse.class);
        PROJECT_IDS_TO_DELETE.add(pr.project.id);

        sendText = "";
        modifyProjectImageMessage.setProjectID(pr.project.id.toString());
        he.onTextMessage(s, gson.toJson(modifyProjectImageMessage));
        ImageResponse imageResponse = new Gson().fromJson(sendText, ImageResponse.class);

        GetProjectMessage getProjectMessage = new GetProjectMessage();
        getProjectMessage.setSessionid(user1.ids.iterator().next().sessionID);
        getProjectMessage.setProjectID(pr.project.id.toString());
        he.onTextMessage(s, gson.toJson(getProjectMessage));
        ProjectResponse pr2 = gson.fromJson(sendText, ProjectResponse.class);
        Assertions.assertNotNull(pr2.project.image);

        modifyProjectImageMessage.setRemoved(true);
        he.onTextMessage(s, gson.toJson(modifyProjectImageMessage));
        he.onTextMessage(s, gson.toJson(getProjectMessage));
        pr2 = gson.fromJson(sendText, ProjectResponse.class);
        Assertions.assertNull(pr2.project.image);

        DB.leakDatabase().getCollection("projects").deleteOne(eq("_id", pr.project.id));
    }

    private class LoginAnswer {
        String sessionid;
    }
}
