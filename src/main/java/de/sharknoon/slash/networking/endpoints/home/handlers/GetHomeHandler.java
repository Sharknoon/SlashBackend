package de.sharknoon.slash.networking.endpoints.home.handlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.Chat;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.handlers.response.HomeResponse;
import de.sharknoon.slash.properties.Properties;
import org.bson.types.ObjectId;

import java.util.Objects;
import java.util.Optional;

public class GetHomeHandler extends HomeEndpointHandler {

    public GetHomeHandler(HomeEndpoint homeEndpoint) {
        super(Status.GET_HOME, homeEndpoint);
    }

    public GetHomeHandler(HomeEndpoint homeEndpoint, HomeEndpointHandler successor) {
        super(Status.GET_HOME, homeEndpoint, successor);
    }

    @Override
    public void messageLogic(StatusAndSessionIDMessage message, User user) {
        HomeResponse home = new HomeResponse();
        home.projects = DB.getProjectsForUser(user);
        home.chats = DB.getNLastChatsForUser(user.id, Properties.getUserConfig().amountfavouritechats());
        for (Chat chat : home.chats) {
            ObjectId partnerID;
            if (Objects.equals(chat.personA, user.id)) {//I am user a
                partnerID = chat.personB;
            } else {
                partnerID = chat.personA;
            }
            Optional<User> u = DB.getUser(partnerID);
            if (u.isPresent()) {
                chat.partnerUsername = u.get().username;
                chat.partnerImage = u.get().image;
            } else {
                chat.partnerUsername = "ERROR";
            }
        }
        homeEndpoint.send(home);
    }

}
