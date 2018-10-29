package de.sharknoon.slash.networking.endpoints.login;


import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.networking.endpoints.Endpoint;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/login")
public class LoginEndpoint extends Endpoint<LoginMessage> {
    
    
    public LoginEndpoint() {
        super(LoginMessage.class);
    }
    
    @Override
    protected void onMessage(Session session, LoginMessage message) {
        String returnMessage;
    
        if (DB.login(message)) {
            returnMessage = "{\"status\":\"OK\",\"message\":\"Successfully logged in\"}";
        } else if (!DB.checkEmailAndUsername(message)) {
            returnMessage = "{\"status\":\"USER_DOES_NOT_EXIST\",\"message\":\"The requested user does not exist\"}";
        } else {
            returnMessage = "{\"status\":\"WRONG_PASSWORD\",\"message\":\"The entered password is not correct\"}";
        }
    
        session.getAsyncRemote().sendText(returnMessage);
    }
    
    private void hashPassword(LoginMessage message) {
    
    }
    
}
