package de.sharknoon.slash.networking.endpoints.home;

import com.google.gson.Gson;
import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.TestSession;
import de.sharknoon.slash.networking.endpoints.login.LoginEndpoint;
import de.sharknoon.slash.networking.endpoints.login.LoginMessage;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.websocket.Session;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

class HomeEndpointTest {

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
        hm.setSessionid(UUID.randomUUID().toString());
        hm.setStatus(HomeEndpoint.GET_USER_STATUS);
        he.onOpen(s);
        he.onMessage(s, hm);
        // ToDo
    }

    @Test
    void test_getChatStatus() {
        Session s = new TestSession(t -> sendText = t);
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
        Session s = new TestSession(t -> sendText = t);
        HomeEndpoint he = new HomeEndpoint();

        HomeMessage hm = new HomeMessage();
        hm.setSessionid(UUID.randomUUID().toString());
        hm.setStatus(HomeEndpoint.ADD_PROJECT_STATUS);
        he.onOpen(s);
        he.onMessage(s, hm);
        // ToDo
    }

    @Test
    void test_getProjectStatus() {
        Session s = new TestSession(t -> sendText = t);
        HomeEndpoint he = new HomeEndpoint();

        HomeMessage hm = new HomeMessage();
        hm.setSessionid(UUID.randomUUID().toString());
        hm.setStatus(HomeEndpoint.GET_PROJECT_STATUS);
        he.onOpen(s);
        he.onMessage(s, hm);
        // ToDo
    }

    @Test
    void test_addMessageStatus() {
        Session s = new TestSession(t -> sendText = t);
        HomeEndpoint he = new HomeEndpoint();

        HomeMessage hm = new HomeMessage();
        hm.setSessionid(UUID.randomUUID().toString());
        hm.setStatus(HomeEndpoint.ADD_MESSAGE_STATUS);
        he.onOpen(s);
        he.onMessage(s, hm);
        // ToDo
    }

    private class LoginAnswer {
        String sessionid;
    }
}
