package de.sharknoon.slash.networking.endpoints.home;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.MessageEmotion;
import de.sharknoon.slash.database.models.MessageType;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.TestSession;
import de.sharknoon.slash.networking.endpoints.login.LoginEndpoint;
import de.sharknoon.slash.networking.endpoints.login.LoginMessage;
import de.sharknoon.slash.networking.utils.LocalDateTimeConverter;
import de.sharknoon.slash.networking.utils.ObjectIdConverter;
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
import java.util.HashSet;
import java.util.UUID;

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
    private static final LoginEndpoint le = new LoginEndpoint();
    private static String sendText = "";
    private static final Session s = new TestSession(t -> sendText = t);

    private static void registerUsers() {
        user1.id = new ObjectId();
        user1.username = UUID.randomUUID().toString().substring(0, 15);
        user1.sessionIDs = new HashSet<>();
        user1.salt = BCrypt.gensalt();
        user1.registrationDate = LocalDateTime.now().withNano(0);
        user1.password = BCrypt.hashpw("123456", user1.salt);
        user1.email = user1.username + "@web.de";
        user1.deviceIDs = new HashSet<>();

        DB.register(user1);

        user2.id = new ObjectId();
        user2.username = UUID.randomUUID().toString().substring(0, 15);
        user2.sessionIDs = new HashSet<>();
        user2.salt = BCrypt.gensalt();
        user2.registrationDate = LocalDateTime.now().withNano(0);
        user2.password = BCrypt.hashpw("123456", user1.salt);
        user2.email = user1.username + "@gmail.com";
        user2.deviceIDs = new HashSet<>();

        DB.register(user2);
    }

    private static void loginUsers(Session s) {
        le.onOpen(s);

        LoginMessage lm = new LoginMessage();
        lm.setUsernameOrEmail(user1.email);
        lm.setPassword("123456");
        lm.setDeviceID("123456789");
        le.onMessage(s, new Gson().toJson(lm));
        LoginAnswer la = new Gson().fromJson(sendText, LoginAnswer.class);
        user1.sessionIDs.add(la.sessionid);

        lm.setUsernameOrEmail(user2.email);
        lm.setPassword("123456");
        lm.setDeviceID("987654321");
        le.onMessage(s, new Gson().toJson(lm));
        la = new Gson().fromJson(sendText, LoginAnswer.class);
        user2.sessionIDs.add(la.sessionid);
    }

    @BeforeAll
    static void setUp() {
        registerUsers();
        loginUsers(s);
    }

    @AfterAll
    static void tearDown() {
        DB.unregister(user1);
        DB.unregister(user2);
    }


    @Test
    void test_getHomeStatus() {
        HomeEndpoint he = new HomeEndpoint();

        StatusAndSessionIDMessage sasm = new StatusAndSessionIDMessage();
        sasm.setSessionid(UUID.randomUUID().toString());
        sasm.setStatus(Status.GET_HOME);
        he.onOpen(s);
        he.onMessage(s, gson.toJson(sasm));

        // Wrong session id
        Assertions.assertEquals("{\"status\":\"NO_LOGIN_OR_TOO_MUCH_DEVICES\",\"messageType\":\"You are either not logged in or using more than 5 devices\"}", sendText);

        // Correct session id
        sasm.setSessionid(user1.sessionIDs.iterator().next());
        he.onMessage(s, gson.toJson(sasm));
        Assertions.assertEquals("{\"status\":\"OK_HOME\",\"projects\":[],\"chats\":[]}", sendText);
    }

    @Test
    void test_getUserStatus() {
        HomeEndpoint he = new HomeEndpoint();

        GetUserMessage getUserMessage = new GetUserMessage();
        getUserMessage.setSessionid(user1.sessionIDs.iterator().next());
        getUserMessage.setStatus(Status.GET_USER);

        he.onOpen(s);
        he.onMessage(s, gson.toJson(getUserMessage));
        Assertions.assertEquals("{\"status\":\"NO_USER_FOUND\",\"description\":\"No user with the specified username was found\"}", sendText);

        getUserMessage.setUsername(user2.username);
        he.onMessage(s, gson.toJson(getUserMessage));
        Assertions.assertTrue(sendText.matches("\\{\"status\":\"OK_USER\",\"user\":\\{\"id\":\"[0-9a-f]+\",\"username\":\"" + user2.username + "\"}}"));
    }

    @Test
    void test_getChatStatus() {
        HomeEndpoint he = new HomeEndpoint();

        GetChatMessage getChatMessage = new GetChatMessage();
        getChatMessage.setSessionid(UUID.randomUUID().toString());
        getChatMessage.setStatus(Status.GET_CHAT);
        getChatMessage.setPartnerUserID("blargh");
        he.onOpen(s);

        he.onMessage(s, gson.toJson(getChatMessage));
        Assertions.assertEquals("{\"status\":\"NO_LOGIN_OR_TOO_MUCH_DEVICES\",\"messageType\":\"You are either not logged in or using more than 5 devices\"}", sendText);

        getChatMessage.setSessionid(user1.sessionIDs.iterator().next());
        he.onMessage(s, gson.toJson(getChatMessage));
        Assertions.assertEquals("{\"status\":\"WRONG_USER_ID\",\"description\":\"The specified userID doesn\\u0027t conform to the right syntax\"}", sendText);

        getChatMessage.setPartnerUserID("5be0abfd0ab4e53cc82294f1");
        he.onMessage(s, gson.toJson(getChatMessage));
        Assertions.assertEquals("{\"status\":\"NO_USER_FOUND\",\"description\":\"No user with the specified id was found\"}", sendText);

        getChatMessage.setPartnerUserID(user2.id.toString());
        he.onMessage(s, gson.toJson(getChatMessage));
        Assertions.assertTrue(sendText.matches("\\{\"status\":\"OK_CHAT\",\"chat\":\\{\"id\":\"[a-f0-9]+\",\"personA\":\"" + user1.id.toString() + "\",\"personB\":\"" + user2.id.toString() + "\",\"partnerUsername\":\"" + user2.username + "\",\"creationDate\":\"\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\",\"messages\":\\[]}}"));

        HomeEndpoint.ChatResponse cr = gson.fromJson(sendText, HomeEndpoint.ChatResponse.class);
        DB.leakDatabase().getCollection("chats").deleteOne(eq("_id", cr.chat.id));
    }

    @Test
    void test_addProjectStatus() {
        HomeEndpoint he = new HomeEndpoint();

        AddProjectMessage addProjectMessage = new AddProjectMessage();
        addProjectMessage.setSessionid(user1.sessionIDs.iterator().next());
        addProjectMessage.setStatus(Status.ADD_PROJECT);
        addProjectMessage.setProjectName("");
        he.onOpen(s);
        he.onMessage(s, gson.toJson(addProjectMessage));
        // Empty project name
        Assertions.assertEquals("{\"status\":\"WRONG_PROJECT_NAME\",\"description\":\"The project name doesn\\u0027t match the specifications\"}", sendText);

        String projectName = UUID.randomUUID().toString().substring(0, 15);
        addProjectMessage.setProjectName(projectName);
        he.onMessage(s, gson.toJson(addProjectMessage));
        //Empty Description
        Assertions.assertEquals("{\"status\":\"WRONG_PROJECT_DESCRIPTION\",\"description\":\"The project description doesn\\u0027t match the specifications\"}", sendText);

        String projectDescription = UUID.randomUUID().toString().substring(0, 15);
        addProjectMessage.setProjectDescription(projectDescription);
        he.onMessage(s, gson.toJson(addProjectMessage));
        Assertions.assertTrue(sendText.startsWith("{\"status\":\"OK_PROJECT\",\"project\":"));
        final String projectStatus = sendText;

        HomeEndpoint.ProjectResponse response = gson.fromJson(sendText, HomeEndpoint.ProjectResponse.class);
        GetProjectMessage getProjectMessage = new GetProjectMessage();
        getProjectMessage.setProjectID(response.project.id.toString());
        getProjectMessage.setStatus(Status.GET_PROJECT);
        getProjectMessage.setSessionid(user1.sessionIDs.iterator().next());
        he.onMessage(s, gson.toJson(getProjectMessage));

        Assertions.assertEquals(projectStatus, sendText);

        DB.leakDatabase().getCollection("projects").deleteMany(eq("name", projectName));
    }

    @Test
    void test_getProjectStatus() {
        HomeEndpoint he = new HomeEndpoint();

        GetProjectMessage getProjectMessage = new GetProjectMessage();
        getProjectMessage.setSessionid(user1.sessionIDs.iterator().next());
        getProjectMessage.setStatus(Status.GET_PROJECT);
        getProjectMessage.setProjectID(StringUtils.EMPTY);
        he.onOpen(s);
        he.onMessage(s, gson.toJson(getProjectMessage));

        Assertions.assertEquals("{\"status\":\"WRONG_PROJECT_ID\",\"description\":\"The specified projectID doesn\\u0027t conform to the right syntax\"}", sendText);

        getProjectMessage.setProjectID("5be0b0dd0fb4e53cc82294f3");
        he.onMessage(s, gson.toJson(getProjectMessage));
        Assertions.assertEquals("{\"status\":\"NO_PROJECT_FOUND\",\"description\":\"No project with the specified id was found\"}", sendText);
    }

    @Test
    void test_addChatMessageStatus() {
        HomeEndpoint he = new HomeEndpoint();

        AddChatMessageMessage addChatMessageMessage = new AddChatMessageMessage();
        addChatMessageMessage.setSessionid(user1.sessionIDs.iterator().next());
        addChatMessageMessage.setStatus(Status.ADD_CHAT_MESSAGE);
        he.onOpen(s);

        he.onMessage(s, gson.toJson(addChatMessageMessage));
        Assertions.assertEquals("{\"status\":\"WRONG_CHAT_ID\",\"description\":\"The syntax of the chat-ID was not correct\"}", sendText);

        addChatMessageMessage.setChatID("5be0a9d584b063292ffb66ce");
        he.onMessage(s, gson.toJson(addChatMessageMessage));
        Assertions.assertEquals("{\"status\":\"NO_CHAT_FOUND\",\"description\":\"No corresponding chat to the chat-ID was found\"}", sendText);

        //constructing a new chat between user1 and user2
        GetChatMessage getChatMessage = new GetChatMessage();
        getChatMessage.setPartnerUserID(user2.id.toString());
        getChatMessage.setSessionid(user1.sessionIDs.iterator().next());
        getChatMessage.setStatus(Status.GET_CHAT);
        he.onMessage(s, gson.toJson(getChatMessage));
        HomeEndpoint.ChatResponse cr = gson.fromJson(sendText, HomeEndpoint.ChatResponse.class);

        addChatMessageMessage.setChatID(cr.chat.id.toString());
        he.onMessage(s, gson.toJson(addChatMessageMessage));
        Assertions.assertEquals("{\"status\":\"CHAT_MESSAGE_TYPE_INVALID\",\"description\":\"The chat message type doesn\\u0027t match the specification\"}", sendText);

        addChatMessageMessage.setMessageType(MessageType.TEXT);
        he.onMessage(s, gson.toJson(addChatMessageMessage));
        Assertions.assertEquals("{\"status\":\"CHAT_MESSAGE_CONTENT_TOO_LONG\",\"description\":\"The chat message content was over 5000 characters long\"}", sendText);

        addChatMessageMessage.setMessageContent("Hallo 123");
        he.onMessage(s, gson.toJson(addChatMessageMessage));
        Assertions.assertTrue(sendText.matches("\\{\"status\":\"OK_CHAT\",\"chat\":\\{\"id\":\"" + cr.chat.id.toString() + "\",\"personA\":\"" + user1.id.toString() + "\",\"personB\":\"" + user2.id.toString() + "\",\"partnerUsername\":\"" + user2.username + "\",\"creationDate\":\"\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\",\"messages\":\\[\\{\"sender\":\"" + user1.id.toString() + "\",\"creationDate\":\"\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\",\"type\":\"TEXT\",\"content\":\"Hallo 123\"}]}}"));

        addChatMessageMessage.setMessageType(MessageType.EMOTION);
        he.onMessage(s, gson.toJson(addChatMessageMessage));
        Assertions.assertEquals("{\"status\":\"CHAT_MESSAGE_SUBJECT_TOO_LONG\",\"description\":\"The chat message subject was too long\"}", sendText);

        addChatMessageMessage.setMessageSubject("Huhu 123");
        he.onMessage(s, gson.toJson(addChatMessageMessage));
        Assertions.assertEquals("{\"status\":\"CHAT_MESSAGE_EMOTION_NOT_SET\",\"description\":\"The chat message emotion was not set\"}", sendText);

        addChatMessageMessage.setMessageEmotion(MessageEmotion.CRITICISM);
        he.onMessage(s, gson.toJson(addChatMessageMessage));
        Assertions.assertTrue(sendText.matches("\\{\"status\":\"OK_CHAT\",\"chat\":\\{\"id\":\"" + cr.chat.id.toString() + "\",\"personA\":\"" + user1.id.toString() + "\",\"personB\":\"" + user2.id.toString() + "\",\"partnerUsername\":\"" + user2.username + "\",\"creationDate\":\"\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\",\"messages\":\\[\\{\"sender\":\"" + user1.id.toString() + "\",\"creationDate\":\"\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\",\"type\":\"EMOTION\",\"content\":\"Hallo 123\",\"subject\":\"Huhu 123\",\"emotion\":\"CRITICISM\"},\\{\"sender\":\"" + user1.id.toString() + "\",\"creationDate\":\"\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\",\"type\":\"TEXT\",\"content\":\"Hallo 123\"}]}}"));

        DB.leakDatabase().getCollection("chats").deleteOne(eq("_id", cr.chat.id));
    }

    @Test
    void test_addProjectMessageStatus() {
        HomeEndpoint he = new HomeEndpoint();

        AddProjectMessageMessage addProjectMessageMessage = new AddProjectMessageMessage();
        addProjectMessageMessage.setSessionid(user1.sessionIDs.iterator().next());
        addProjectMessageMessage.setStatus(Status.ADD_PROJECT_MESSAGE);
        he.onOpen(s);

        he.onMessage(s, gson.toJson(addProjectMessageMessage));
        Assertions.assertEquals("{\"status\":\"WRONG_PROJECT_ID\",\"description\":\"The syntax of the project-ID was not correct\"}", sendText);

        addProjectMessageMessage.setProjectID("5be0a9d584b063292ffb66ce");
        he.onMessage(s, gson.toJson(addProjectMessageMessage));
        Assertions.assertEquals("{\"status\":\"NO_PROJECT_FOUND\",\"description\":\"No corresponding project to the project-ID was found\"}", sendText);

        //constructing a new project
        AddProjectMessage addProjectMessage = new AddProjectMessage();
        addProjectMessage.setProjectName("Test123 Project");
        addProjectMessage.setProjectDescription("I am going to be deleted very soon");
        addProjectMessage.setSessionid(user1.sessionIDs.iterator().next());
        addProjectMessage.setStatus(Status.ADD_PROJECT);
        he.onMessage(s, gson.toJson(addProjectMessage));
        HomeEndpoint.ProjectResponse pr = gson.fromJson(sendText, HomeEndpoint.ProjectResponse.class);

        addProjectMessageMessage.setProjectID(pr.project.id.toString());
        he.onMessage(s, gson.toJson(addProjectMessageMessage));
        Assertions.assertEquals("{\"status\":\"PROJECT_MESSAGE_TYPE_INVALID\",\"description\":\"The project message type doesn\\u0027t match the specification\"}", sendText);

        addProjectMessageMessage.setMessageType(MessageType.TEXT);
        he.onMessage(s, gson.toJson(addProjectMessageMessage));
        Assertions.assertEquals("{\"status\":\"PROJECT_MESSAGE_CONTENT_TOO_LONG\",\"description\":\"The project message content was over 5000 characters long\"}", sendText);

        addProjectMessageMessage.setMessageContent("Hallo 123");
        he.onMessage(s, gson.toJson(addProjectMessageMessage));
        Assertions.assertTrue(sendText.matches("\\{\"status\":\"OK_PROJECT\",\"project\":\\{\"id\":\"" + pr.project.id.toString() + "\",\"name\":\"Test123 Project\",\"description\":\"I am going to be deleted very soon\",\"image\":\"https://www\\.myfloridacfo\\.com/division/oit/images/DIS-HomeResponse\\.png\",\"creationDate\":\"\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\",\"users\":\\[\"" + user1.id.toString() + "\"],\"messages\":\\[\\{\"sender\":\"" + user1.id.toString() + "\",\"creationDate\":\"\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\",\"type\":\"TEXT\",\"content\":\"Hallo 123\"}]}}"));

        addProjectMessageMessage.setMessageType(MessageType.EMOTION);
        he.onMessage(s, gson.toJson(addProjectMessageMessage));
        Assertions.assertEquals("{\"status\":\"PROJECT_MESSAGE_SUBJECT_TOO_LONG\",\"description\":\"The project message subject was too long\"}", sendText);

        addProjectMessageMessage.setMessageSubject("Huhu 123");
        he.onMessage(s, gson.toJson(addProjectMessageMessage));
        Assertions.assertEquals("{\"status\":\"PROJECT_MESSAGE_EMOTION_NOT_SET\",\"description\":\"The project message emotion was not set\"}", sendText);

        addProjectMessageMessage.setMessageEmotion(MessageEmotion.CRITICISM);
        he.onMessage(s, gson.toJson(addProjectMessageMessage));
        //Assertions.assertTrue(sendText.matches("\\{\"status\":\"OK_PROJECT\",\"project\":\\{\"id\":\"" + pr.project.id.toString() + "\",\"name\":\"Test123 Project\",\"description\":\"I am going to be deleted very soon\",\"image\":\"https://www.myfloridacfo.com/division/oit/images/DIS-HomeResponse.png\",\"creationDate\":\"\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\",\"users\":\\[\"" + user1.id.toString() + "\"],\"messages\":\\[\\{\"sender\":\"" + user1.id.toString() + "\",\"creationDate\":\"\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\",\"type\":\"EMOTION\",\"content\":\"Hallo 123\",\"subject\":\"Huhu 123\",\"emotion\":\"CRITICISM\"},\\{\"sender\":\"" + user1.id.toString() + "\",\"creationDate\":\"\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\",\"type\":\"TEXT\",\"content\":\"Hallo 123\"}]}}"));
        Assertions.assertTrue(sendText.matches("\\{\"status\":\"OK_PROJECT\",\"project\":\\{\"id\":\".*\",\"name\":\"Test123 Project\",\"description\":\"I am going to be deleted very soon\",\"image\":\"https://www.myfloridacfo.com/division/oit/images/DIS-HomeResponse.png\",\"creationDate\":\"\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\",\"users\":\\[\".*\"],\"messages\":\\[\\{\"sender\":\".*\",\"creationDate\":\"\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\",\"type\":\"EMOTION\",\"content\":\"Hallo 123\",\"subject\":\"Huhu 123\",\"emotion\":\"CRITICISM\"},\\{\"sender\":\".*\",\"creationDate\":\"\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}\",\"type\":\"TEXT\",\"content\":\"Hallo 123\"}]}}"));

        DB.leakDatabase().getCollection("projects").deleteOne(eq("_id", pr.project.id));
    }

    private class LoginAnswer {
        String sessionid;
    }
}
