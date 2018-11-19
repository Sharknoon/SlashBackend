package de.sharknoon.slash.networking.endpoints.home;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.TestSession;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.websocket.Session;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

class HomeEndpointTest {

    private static final String sessionId = UUID.randomUUID().toString();
    private static User user;
    private String sendText = "";


    @BeforeAll
    static void setUp() {
        user = new User();
        user.id = new ObjectId();
        user.username = UUID.randomUUID().toString().substring(0, 15);
        user.sessionIDs = Collections.singleton(sessionId);
        user.salt = BCrypt.gensalt();
        user.registrationDate = LocalDateTime.now();
        user.password = BCrypt.hashpw("123456", user.salt);
        user.email = user.username + "@web.de";

        DB.register(user);
    }
    
    @AfterAll
    static void tearDown() {
        DB.unregister(user);
    }


    @Test
    void test_getHomeStatus() {
        Session s = new TestSession(t -> sendText = t);
        HomeEndpoint he = new HomeEndpoint();

        HomeMessage hm = new HomeMessage();
        // ToDo
        // Wrong session id
        hm.setSessionid(sessionId);
        hm.setStatus(HomeEndpoint.GET_HOME_STATUS);
        he.onMessage(s, hm);
        Assertions.assertEquals("", sendText);
    }

    @Test
    void test_getUserStatus() {
        Session s = new TestSession(t -> sendText = t);
        HomeEndpoint he = new HomeEndpoint();

        HomeMessage hm = new HomeMessage();
        // ToDo
    }

    @Test
    void test_getChatStatus() {
        Session s = new TestSession(t -> sendText = t);
        HomeEndpoint he = new HomeEndpoint();

        HomeMessage hm = new HomeMessage();
        // ToDo
    }

    @Test
    void test_addProjectStatus() {
        Session s = new TestSession(t -> sendText = t);
        HomeEndpoint he = new HomeEndpoint();

        HomeMessage hm = new HomeMessage();
        // ToDo
    }

    @Test
    void test_getProjectStatus() {
        Session s = new TestSession(t -> sendText = t);
        HomeEndpoint he = new HomeEndpoint();

        HomeMessage hm = new HomeMessage();
        // ToDo
    }

    @Test
    void test_addMessageStatus() {
        Session s = new TestSession(t -> sendText = t);
        HomeEndpoint he = new HomeEndpoint();

        HomeMessage hm = new HomeMessage();
        // ToDo
    }
}
