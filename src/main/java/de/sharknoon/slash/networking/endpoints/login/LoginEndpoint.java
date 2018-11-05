package de.sharknoon.slash.networking.endpoints.login;


import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.LoginSessions;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import de.sharknoon.slash.properties.Properties;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.*;

@ServerEndpoint("/login")
public class LoginEndpoint extends Endpoint<LoginMessage> {
    
    private static final String SESSION = "id";
    private static final String USER = "user";
    
    public LoginEndpoint() {
        super(LoginMessage.class);
    }
    
    private static void hashPasswordOnLogin(LoginMessage message, String salt) {
        String saltedPassword = BCrypt.hashpw(message.getPassword(), salt);
        message.setPassword(saltedPassword);
    }
    
    @Override
    protected void onMessage(Session session, LoginMessage message) {
        String returnMessage;
        Optional<User> optionalUser = Optional.empty();
    
        if (session.getUserProperties().containsKey(SESSION)) {
            returnMessage = "{\"status\":\"USER_ALREADY_LOGGED_IN\",\"message\":\"The requested user is already logged in\"}";
        } else if ((optionalUser = DB.login(message.getUsernameOrEmail())).isPresent() && login(optionalUser.get(), message)) {
            User user = optionalUser.get();
            session.getUserProperties().put(USER, user);
        
            String sessionID = generateSessionID();
            session.getUserProperties().put(SESSION, sessionID);
            
            returnMessage = "{\"status\":\"OK\",\"message\":\"Successfully logged in\",\"sessionid\":\"" + sessionID + "\"}";
        } else if (!DB.existsEmailOrUsername(message)) {
            returnMessage = "{\"status\":\"USER_DOES_NOT_EXIST\",\"message\":\"The requested user does not exist\"}";
        } else {
            returnMessage = "{\"status\":\"WRONG_PASSWORD\",\"message\":\"The entered password is not correct\"}";
        }
    
        session.getAsyncRemote().sendText(returnMessage);
    
        //Do it here instead in the if above to send the response to the client as quickly as possible
        if (optionalUser.isPresent()) {
            String sessionID = (String) session.getUserProperties().get(SESSION);
            User user = (User) session.getUserProperties().get(USER);
            while (user.sessionIDs.size() > Properties.getUserConfig().maxdevices()) {
                user.sessionIDs.remove(user.sessionIDs.iterator().next());
            }
            user.sessionIDs.add(sessionID);
            DB.addSessionID(user, sessionID);
            LoginSessions.addSession(user, sessionID, LoginEndpoint.class, session);
        }
    }
    
    private boolean login(User user, LoginMessage message) {
        String salt = user.salt;
        hashPasswordOnLogin(message, salt);
        return message.getPassword().equals(user.password);
    }
    
    
    private String generateSessionID() {
        return UUID.randomUUID().toString();
    }
    
    
    private void logout(Session session) {
        String sessionID = (String) session.getUserProperties().get(SESSION);
        User user = (User) session.getUserProperties().get(USER);
        DB.removeSessionID(user, sessionID);
        LoginSessions.removeSession(sessionID);
    }
}
