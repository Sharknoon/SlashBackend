package de.sharknoon.slash.networking.endpoints.home.messagehandlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.Chat;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.HomeResponse;
import de.sharknoon.slash.properties.Properties;

import java.util.Objects;

public class GetHomeMessageHandler extends HomeEndpointMessageHandler {

    public GetHomeMessageHandler(HomeEndpoint homeEndpoint) {
        super(Status.GET_HOME, homeEndpoint);
    }
    public GetHomeMessageHandler(HomeEndpoint homeEndpoint, HomeEndpointMessageHandler successor) {
        super(Status.GET_HOME, homeEndpoint, successor);
    }

    @Override
    public void messageLogic(StatusAndSessionIDMessage message, User user) {
        HomeResponse home = new HomeResponse();
        home.projects = DB.getProjectsForUser(user);
        home.chats = DB.getNLastChatsForUser(user.id, Properties.getUserConfig().amountfavouritechats());
        for (Chat chat : home.chats) {
            if (Objects.equals(chat.personA, user.id)) {//I am user a
                chat.partnerUsername = DB.getUser(chat.personB).map(u -> u.username).orElse("ERROR");
            } else {
                chat.partnerUsername = DB.getUser(chat.personA).map(u -> u.username).orElse("ERROR");
            }
        }
        homeEndpoint.send(home);
    }

}
