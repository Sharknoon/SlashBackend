package de.sharknoon.slash.networking.endpoints.home;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.sharknoon.slash.database.DB;
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

class HomeEndpointTest {
    private static Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeConverter())
            .registerTypeAdapter(ObjectId.class, new ObjectIdConverter())
            .excludeFieldsWithModifiers(Modifier.STATIC)
            .create();

    private static final User user = new User();
    private static LoginEndpoint le = new LoginEndpoint();
    private static String sendText = "";
    private static Session s = new TestSession(t -> sendText = t);

    private static void registerUser() {
        user.id = new ObjectId();
        user.username = UUID.randomUUID().toString().substring(0, 15);
        user.sessionIDs = new HashSet<>();
        user.salt = BCrypt.gensalt();
        user.registrationDate = LocalDateTime.now();
        user.password = BCrypt.hashpw("123456", user.salt);
        user.email = user.username + "@web.de";

        DB.register(user);
    }

    private static void loginUser(Session s) {
        le.onOpen(s);
        LoginMessage lm = new LoginMessage();
        lm.setUsernameOrEmail(user.email);
        lm.setPassword("123456");
        le.onMessage(s, new Gson().toJson(lm));
        LoginAnswer la = new Gson().fromJson(sendText, LoginAnswer.class);
        user.sessionIDs.add(la.sessionid);
    }

    @BeforeAll
    static void setUp() {
        registerUser();
        loginUser(s);
    }
    
    @AfterAll
    static void tearDown() {
        DB.unregister(user);
    }


    @Test
    void test_getHomeStatus() {
        HomeEndpoint he = new HomeEndpoint();

        HomeMessage hm = new HomeMessage();
        hm.setSessionid(UUID.randomUUID().toString());
        hm.setStatus(HomeEndpoint.GET_HOME_STATUS);
        he.onOpen(s);
        he.onMessage(s, hm);

        // Wrong session id
        Assertions.assertEquals("{\"status\":\"NO_LOGIN_OR_TOO_MUCH_DEVICES\",\"message\":\"You are either not logged in or using more than 5 devices\"}", sendText);

        // Correct session id
        hm.setSessionid(user.sessionIDs.iterator().next());
        he.onMessage(s, hm);
        Assertions.assertEquals("{\"status\":\"OK_HOME\",\"projects\":[],\"chats\":[]}", sendText);
    }

    @Test
    void test_getUserStatus() {
        HomeEndpoint he = new HomeEndpoint();

        HomeMessage hm = new HomeMessage();
        hm.setSessionid(user.sessionIDs.iterator().next());
        hm.setStatus(HomeEndpoint.GET_USER_STATUS);
        he.onOpen(s);
        he.onMessage(s, hm);
        // ToDo
    }

    @Test
    void test_getChatStatus() {
        HomeEndpoint he = new HomeEndpoint();

        HomeMessage hm = new HomeMessage();
        hm.setSessionid(UUID.randomUUID().toString());
        hm.setStatus(HomeEndpoint.GET_CHAT_STATUS);
        he.onOpen(s);
        he.onMessage(s, hm);
        // ToDo
    }

    @Test
    void test_addProjectStatus() {
        HomeEndpoint he = new HomeEndpoint();

        HomeMessage hm = new HomeMessage();
        hm.setSessionid(user.sessionIDs.iterator().next());
        hm.setStatus(HomeEndpoint.ADD_PROJECT_STATUS);
        hm.setProjectName("");
        he.onOpen(s);
        he.onMessage(s, hm);
        // Empty project name
        Assertions.assertEquals("{\"status\":\"WRONG_PROJECT_NAME\",\"description\":\"The project name doesn\\u0027t match the specifications\"}", sendText);

        hm.setProjectName("Project NEW");
        he.onMessage(s, hm);
        Assertions.assertTrue(sendText.startsWith("{\"status\":\"OK_PROJECT\",\"project\":"));
        final String projectStatus = sendText;

        HomeEndpoint.ProjectResponse response = gson.fromJson(sendText, HomeEndpoint.ProjectResponse.class);
        hm.setProjectID(response.project.id.toString());
        hm.setStatus(HomeEndpoint.GET_PROJECT_STATUS);
        he.onMessage(s, hm);

        Assertions.assertEquals(projectStatus, sendText);
    }

    @Test
    void test_getProjectStatus() {
        HomeEndpoint he = new HomeEndpoint();

        HomeMessage hm = new HomeMessage();
        hm.setSessionid(user.sessionIDs.iterator().next());
        hm.setStatus(HomeEndpoint.GET_PROJECT_STATUS);
        hm.setProjectID(StringUtils.EMPTY);
        he.onOpen(s);
        he.onMessage(s, hm);

        Assertions.assertEquals("{\"status\":\"WRONG_PROJECT_ID\",\"description\":\"The specified projectID doesn\\u0027t conform to the right syntax\"}", sendText);

        hm.setProjectID("5be0b0dd0fb4e53cc82294f3");
        he.onMessage(s, hm);
        Assertions.assertEquals("{\"status\":\"NO_PROJECT_FOUND\",\"description\":\"No project with the specified id was found\"}", sendText);
    }

    @Test
    void test_addMessageStatus() {
        HomeEndpoint he = new HomeEndpoint();

        HomeMessage hm = new HomeMessage();
        hm.setSessionid(user.sessionIDs.iterator().next());
        hm.setStatus(HomeEndpoint.ADD_MESSAGE_STATUS);
        he.onOpen(s);
        he.onMessage(s, hm);
        // ToDo
    }

    private class LoginAnswer {
        String sessionid;
    }
}
