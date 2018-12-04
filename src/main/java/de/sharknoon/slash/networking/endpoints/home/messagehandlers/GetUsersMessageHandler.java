package de.sharknoon.slash.networking.endpoints.home.messagehandlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.Status;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.UsersResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.GetUsersMessage;
import de.sharknoon.slash.networking.endpoints.home.messages.StatusAndSessionIDMessage;
import de.sharknoon.slash.serialisation.Serialisation;

import java.util.Set;

public class GetUsersMessageHandler extends HomeEndpointMessageHandler {

    public GetUsersMessageHandler(HomeEndpoint homeEndpoint) {
        super(Status.GET_USERS, homeEndpoint);
    }

    public GetUsersMessageHandler(HomeEndpoint homeEndpoint, HomeEndpointMessageHandler successor) {
        super(Status.GET_USERS, homeEndpoint, successor);
    }

    @Override
    public void messageLogic(StatusAndSessionIDMessage message, User user) {
        GetUsersMessage getUsersMessage = Serialisation.getGSON().fromJson(homeEndpoint.getLastMessage(), GetUsersMessage.class);
        Set<User> foundUsers = DB.searchUsers(getUsersMessage.getSearch());
        UsersResponse um = new UsersResponse();
        um.users = foundUsers;
        homeEndpoint.send(um);
    }
}
