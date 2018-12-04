package de.sharknoon.slash.networking.endpoints.home.messagehandlers;

import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.Status;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.ErrorResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.StatusAndSessionIDMessage;

public class NoneStatusMessageHandler extends HomeEndpointMessageHandler {

    public NoneStatusMessageHandler(HomeEndpoint homeEndpoint) {
        super(Status.NONE, homeEndpoint);
    }

    public NoneStatusMessageHandler(HomeEndpoint homeEndpoint, HomeEndpointMessageHandler successor) {
        super(Status.NONE, homeEndpoint, successor);
    }

    @Override
    protected void messageLogic(StatusAndSessionIDMessage message, User user) {
        ErrorResponse error = new ErrorResponse();
        error.status = "WRONG_STATUS";
        error.description = "The status was wrong, please check the API";
        homeEndpoint.send(error);
    }
}
