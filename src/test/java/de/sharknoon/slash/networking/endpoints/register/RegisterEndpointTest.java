package de.sharknoon.slash.networking.endpoints.register;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.networking.endpoints.TestSession;
import org.junit.jupiter.api.Test;

import javax.websocket.Session;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RegisterEndpointTest {
    
    private String sendText = "";
    
    @Test
    void onMessage() {
        
        
        RegisterEndpoint re = new RegisterEndpoint();
        
        Session testSession = new TestSession(t -> sendText = t);
        
        RegisterMessage rm = new RegisterMessage();
        rm.setPassword("123456");
        String reallyRandomMail = UUID.randomUUID().toString() + "@blub.de";
        rm.setEmail(reallyRandomMail);
        String reallyRandomUsername = UUID.randomUUID().toString().substring(0, 15);
        rm.setUsername(reallyRandomUsername);
        
        //normal registration
        re.onMessage(testSession, rm);
        assertEquals("{\"status\":\"OK\",\"message\":\"Successfully registered\"}", sendText);
        
        //same registration again
        re.onMessage(testSession, rm);
        assertEquals("{\"status\":\"USERNAME_ALREADY_REGISTERED\",\"message\":\"The specified username is already taken by another account. Please choose another one.\"}", sendText);
        
        //test caseinsensitivity on the username
        rm.setUsername(rm.getUsername().toUpperCase());
        re.onMessage(testSession, rm);
        assertEquals("{\"status\":\"USERNAME_ALREADY_REGISTERED\",\"message\":\"The specified username is already taken by another account. Please choose another one.\"}", sendText);
        
        //test email duplicate
        rm.setUsername(UUID.randomUUID().toString().substring(0, 15));
        re.onMessage(testSession, rm);
        assertEquals("{\"status\":\"EMAIL_ALREADY_REGISTERED\",\"message\":\"The specified email is already taken by another account. Please log in.\"}", sendText);
        
        //test email duplicate with case insensitivity
        rm.setEmail(rm.getEmail().toUpperCase());
        re.onMessage(testSession, rm);
        assertEquals("{\"status\":\"EMAIL_ALREADY_REGISTERED\",\"message\":\"The specified email is already taken by another account. Please log in.\"}", sendText);
        
        //test too short username
        rm.setEmail(UUID.randomUUID().toString() + "@web.de");
        rm.setUsername("");
        re.onMessage(testSession, rm);
        assertEquals("{\"status\":\"WRONG_USERNAME\",\"message\":\"The specified username does not meet the specifications\"}", sendText);
        
        //test too long username
        rm.setUsername("123456789012345678921234");
        re.onMessage(testSession, rm);
        assertEquals("{\"status\":\"WRONG_USERNAME\",\"message\":\"The specified username does not meet the specifications\"}", sendText);
        
        //test wrong mail
        rm.setEmail("web.de");
        rm.setUsername("9012345678921");
        re.onMessage(testSession, rm);
        assertEquals("{\"status\":\"WRONG_EMAIL\",\"message\":\"The specified email does not meet the specifications\"}", sendText);
        
        //clean db
        DB.leakDatabase().getCollection("users").deleteOne(eq("email", reallyRandomMail));
    }
}