package de.sharknoon.slash.networking.endpoints.home.handlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.handlers.response.UsersResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.GetUsersMessage;
import de.sharknoon.slash.serialisation.Serialisation;

import java.util.Set;

public class GetUsersHandler extends HomeEndpointHandler {

    public GetUsersHandler(HomeEndpoint homeEndpoint) {
        super(Status.GET_USERS, homeEndpoint);
    }

    public GetUsersHandler(HomeEndpoint homeEndpoint, HomeEndpointHandler successor) {
        super(Status.GET_USERS, homeEndpoint, successor);
    }

    @Override
    public void messageLogic(StatusAndSessionIDMessage message, User user) {
        GetUsersMessage getUsersMessage = Serialisation.getGSON().fromJson(homeEndpoint.getLastTextMessage(), GetUsersMessage.class);
        Set<User> foundUsers = DB.searchUsers(getUsersMessage.getSearch());
        UsersResponse um = new UsersResponse();
        um.users = foundUsers;
        homeEndpoint.send(um);
    }
}
