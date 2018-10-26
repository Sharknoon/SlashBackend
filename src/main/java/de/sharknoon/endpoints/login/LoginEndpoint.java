package de.sharknoon.endpoints.login;


import de.sharknoon.endpoints.Endpoint;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/login")
public class LoginEndpoint extends Endpoint<LoginMessage> {
    
    
    public LoginEndpoint() {
        super(LoginMessage.class);
    }
    
    @Override
    protected void onMessage(Session session, LoginMessage message) {
        session.getAsyncRemote().sendText("received:\n" + GSON.toJson(message));
    }
}
