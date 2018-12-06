package de.sharknoon.slash.networking.endpoints.login;


import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import de.sharknoon.slash.networking.sessions.LoginSessions;
import de.sharknoon.slash.properties.Properties;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.*;

@ServerEndpoint("/login")
public class LoginEndpoint extends Endpoint<LoginMessage> {
    
    private static final String SESSION = "session";
    private static final String DEVICE = "device";
    private static final String USER = "user";
    
    //Needs to stay public
    public LoginEndpoint() {
        super(LoginMessage.class);
    }
    
    @Override
    protected void onTextMessage(Session session, LoginMessage message) {
        String returnMessage;
        Optional<User> optionalUser = Optional.empty();
        
        if (session.getUserProperties().containsKey(SESSION)) {
            returnMessage = "{\"status\":\"USER_ALREADY_LOGGED_IN\",\"message\":\"The requested user is already logged in\"}";
        } else if ((optionalUser = login(message)).isPresent()) {
            User user = optionalUser.get();
            session.getUserProperties().put(USER, user);
            
            String sessionID = generateSessionID();
            session.getUserProperties().put(SESSION, sessionID);
            
            String deviceID = message.getDeviceID();
            session.getUserProperties().put(DEVICE, deviceID);
            
            returnMessage = "{\"status\":\"OK\",\"message\":\"Successfully logged in\",\"sessionid\":\"" + sessionID + "\",\"userID\":\"" + user.id + "\"}";
        } else if (message.getDeviceID().isEmpty()) {
            returnMessage = "{\"status\":\"MISSING_DEVICE_ID\",\"message\":\"The deviceID for this device is missing\"}";
        } else if (!DB.existsEmailOrUsername(message.getUsernameOrEmail())) {
            returnMessage = "{\"status\":\"USER_DOES_NOT_EXIST\",\"message\":\"The requested user does not exist\"}";
        } else {
            returnMessage = "{\"status\":\"WRONG_PASSWORD\",\"message\":\"The entered password is not correct\"}";
        }
        
        session.getAsyncRemote().sendText(returnMessage);
        
        //Do it here instead in the if above to send the response to the client as quickly as possible
        if (optionalUser.isPresent()) {
            String sessionID = (String) session.getUserProperties().get(SESSION);
            String deviceID = (String) session.getUserProperties().get(DEVICE);
            User user = (User) session.getUserProperties().get(USER);
    
            while (user.ids.size() >= Properties.getUserConfig().maxdevices()) {
                user.ids.remove(user.ids.keySet().iterator().next());
            }
            user.ids.put(deviceID, sessionID);
            DB.registerDeviceID(user, deviceID, sessionID);
    
            LoginSessions.addSession(user, sessionID, LoginEndpoint.class, session);
        }
    }
    
    @Override
    protected void onBinaryMessage(Session session, byte[] binary) {
        //Dont expect any binary messages
    }
    
    private Optional<User> login(LoginMessage message) {
        if (message.getDeviceID().isEmpty()) {
            return Optional.empty();
        }
        
        Optional<User> user = DB.getUserByUsernameOrEmail(message.getUsernameOrEmail());
        if (user.isEmpty()) {
            return user;
        }
        
        String salt = user.get().salt;
        String hashedPW = BCrypt.hashpw(message.getPassword(), salt);
        if (hashedPW.equals(user.get().password)) {
            return user;
        }
        return Optional.empty();
    }
    
    
    private String generateSessionID() {
        return UUID.randomUUID().toString();
    }
    
}
