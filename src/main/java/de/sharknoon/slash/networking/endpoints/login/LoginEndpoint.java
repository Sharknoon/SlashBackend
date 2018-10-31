package de.sharknoon.slash.networking.endpoints.login;


import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.LoginSessions;
import de.sharknoon.slash.networking.endpoints.Endpoint;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.*;

@ServerEndpoint("/login")
public class LoginEndpoint extends Endpoint<LoginMessage> {
    
    private static final String SESSION_ID = "id";
    
    public LoginEndpoint() {
        super(LoginMessage.class);
    }
    
    @Override
    protected void onMessage(Session session, LoginMessage message) {
        String returnMessage;
        Optional<User> optionalUser = Optional.empty();
    
        String sessionID = "";
        if (session.getUserProperties().containsKey(SESSION_ID)) {
            returnMessage = "{\"status\":\"USER_ALREADY_LOGGED_IN\",\"message\":\"The requested user is already logged in\"}";
        } else if ((optionalUser = DB.login(message)).isPresent()) {
            sessionID = UUID.randomUUID().toString();
            session.getUserProperties().put(SESSION_ID, sessionID);
            returnMessage = "{\"status\":\"OK\",\"message\":\"Successfully logged in\",\"sessionid\":\"" + sessionID + "\"}";
        } else if (!DB.existsEmailOrUsername(message)) {
            returnMessage = "{\"status\":\"USER_DOES_NOT_EXIST\",\"message\":\"The requested user does not exist\"}";
        } else {
            returnMessage = "{\"status\":\"WRONG_PASSWORD\",\"message\":\"The entered password is not correct\"}";
        }
    
        session.getAsyncRemote().sendText(returnMessage);
    
        //Do it here instead in the if above to send the response to the client as quickly as possible
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            LoginSessions.addSession(user, sessionID);
        }
    }
    
    @Override
    public void onClose(Session session, CloseReason closeReason) {
        super.onClose(session, closeReason);
        LoginSessions.removeSession(String.valueOf(session.getUserProperties().get(SESSION_ID)));
    }
    
    @Override
    public void onError(Session session, Throwable throwable) {
        super.onError(session, throwable);
        LoginSessions.removeSession(String.valueOf(session.getUserProperties().get(SESSION_ID)));
    }
}
