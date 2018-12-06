package de.sharknoon.slash.networking.endpoints.login;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.TestSession;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.websocket.Session;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LoginEndpointTest {
    
    private static User user;
    
    @BeforeAll
    static void setUp() {
        user = new User();
        user.id = new ObjectId();
        user.username = UUID.randomUUID().toString().substring(0, 15);
        user.ids = new HashMap<>();
        user.salt = BCrypt.gensalt();
        user.registrationDate = LocalDateTime.now().withNano(0);
        user.password = BCrypt.hashpw("123456", user.salt);
        user.email = user.username + "@web.de";
        
        DB.register(user);
    }
    
    @AfterAll
    static void tearDown() {
        DB.unregister(user);
    }
    
    private String sendText = "";
    
    @Test
    void onMessage() {
        
        Session s = new TestSession(t -> sendText = t);
        LoginEndpoint le = new LoginEndpoint();
        
        LoginMessage lm = new LoginMessage();
    
        //missing device id
        lm.setPassword("");
        lm.setUsernameOrEmail(user.email);
        le.onTextMessage(s, lm);
        assertEquals("{\"status\":\"MISSING_DEVICE_ID\",\"message\":\"The deviceID for this device is missing\"}", sendText);
    
        //wrong pw
        lm.setDeviceID("123456789");
        le.onTextMessage(s, lm);
        assertEquals("{\"status\":\"WRONG_PASSWORD\",\"message\":\"The entered password is not correct\"}", sendText);
        
        //wrong pw with other email
        lm.setUsernameOrEmail(user.email.toUpperCase());
        le.onTextMessage(s, lm);
        assertEquals("{\"status\":\"WRONG_PASSWORD\",\"message\":\"The entered password is not correct\"}", sendText);
        
        //wrong pw with username
        lm.setUsernameOrEmail(user.username);
        le.onTextMessage(s, lm);
        assertEquals("{\"status\":\"WRONG_PASSWORD\",\"message\":\"The entered password is not correct\"}", sendText);
        
        //wrong pw with other username
        lm.setUsernameOrEmail(user.username.toUpperCase());
        le.onTextMessage(s, lm);
        assertEquals("{\"status\":\"WRONG_PASSWORD\",\"message\":\"The entered password is not correct\"}", sendText);
        
        //user doesn't exist
        lm.setUsernameOrEmail(UUID.randomUUID().toString().substring(0, 15));
        lm.setPassword("123456");
        le.onTextMessage(s, lm);
        assertEquals("{\"status\":\"USER_DOES_NOT_EXIST\",\"message\":\"The requested user does not exist\"}", sendText);
    
        //normal getUserID
        lm.setUsernameOrEmail(user.email.toUpperCase());
        le.onTextMessage(s, lm);
        assertTrue(sendText.startsWith("{\"status\":\"OK\",\"message\":\"Successfully logged in\",\"sessionid\":\""));
    
        //duplicate getUserID
        le.onTextMessage(s, lm);
        assertEquals("{\"status\":\"USER_ALREADY_LOGGED_IN\",\"message\":\"The requested user is already logged in\"}", sendText);
    }
}