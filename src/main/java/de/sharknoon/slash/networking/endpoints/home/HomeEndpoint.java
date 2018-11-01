package de.sharknoon.slash.networking.endpoints.home;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.*;
import de.sharknoon.slash.networking.LoginSessions;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import de.sharknoon.slash.properties.Properties;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.*;

@ServerEndpoint("/home")
public class HomeEndpoint extends Endpoint<HomeMessage> {
    
    public HomeEndpoint() {
        super(HomeMessage.class);
    }
    
    @Override
    protected void onMessage(Session session, HomeMessage message) {
        Optional<User> user = LoginSessions.getUser(message.getSessionid());
        
        String returnMessage;
        
        if (!user.isPresent()) {//To be replaced with isEmpty, this is because intellij shows a warning because it doesnt know the new isEmtpy()
            returnMessage = "{\"status\":\"NO_LOGIN_OR_TOO_MUCH_DEVICES\"," +
                    "\"message\":\"You are either not logged in or using more than " +
                    Properties.getUserConfig().maxdevices() + " devices\"}";
        } else {
            Home home = new Home();
            home.projects = DB.getProjectsForUser(user.get());
            home.chats = DB.getNLastChatsForUser(user.get(), 6);
            try {
                returnMessage = GSON.toJson(home);
            } catch (Exception e) {
                returnMessage = "{\"status\":\"ERROR\"," +
                        "\"message\":\"An unexpected error occurred, please try again later\"}";
            }
        }
        
        session.getAsyncRemote().sendText(returnMessage);
    }
    
    private class Home {
        Set<Project> projects;
        Set<Chat> chats;
    }
}
