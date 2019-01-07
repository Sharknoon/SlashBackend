package de.sharknoon.slash.networking.endpoints.home.messagehandlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.ErrorResponse;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.UserResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.GetUserMessage;
import de.sharknoon.slash.serialisation.Serialisation;
import org.bson.types.ObjectId;

import java.util.Optional;

public class GetUserMessageHandler extends HomeEndpointMessageHandler {

    public GetUserMessageHandler(HomeEndpoint homeEndpoint) {
        super(Status.GET_USER, homeEndpoint);
    }

    public GetUserMessageHandler(HomeEndpoint homeEndpoint, HomeEndpointMessageHandler successor) {
        super(Status.GET_USER, homeEndpoint, successor);
    }

    @Override
    public void messageLogic(StatusAndSessionIDMessage message, User user) {
        GetUserMessage getUserMessage = Serialisation.getGSON().fromJson(homeEndpoint.getLastTextMessage(), GetUserMessage.class);
        String userID = getUserMessage.getUserID();
        Optional<User> optionalUser;
        if (!ObjectId.isValid(userID)) {
            ErrorResponse error = new ErrorResponse();
            error.status = "NO_USER_FOUND";
            error.description = "No user with the specified id was found";
            homeEndpoint.send(error);
            return;
        } else if ((optionalUser = DB.getUser(new ObjectId(userID))).isEmpty()) {
            ErrorResponse error = new ErrorResponse();
            error.status = "NO_USER_FOUND";
            error.description = "No user with the specified id was found";
            homeEndpoint.send(error);
            return;
        }
        UserResponse ur = new UserResponse();
        ur.user = optionalUser.get();
        homeEndpoint.send(ur);
    }
}
