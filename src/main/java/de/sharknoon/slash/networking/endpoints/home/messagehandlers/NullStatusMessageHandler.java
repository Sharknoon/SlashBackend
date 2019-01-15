package de.sharknoon.slash.networking.endpoints.home.messagehandlers;

import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.ErrorResponse;

public class NullStatusMessageHandler extends HomeEndpointMessageHandler {

    public NullStatusMessageHandler(HomeEndpoint homeEndpoint) {
        super(null, homeEndpoint);
    }

    public NullStatusMessageHandler(HomeEndpoint homeEndpoint, HomeEndpointMessageHandler successor) {
        super(null, homeEndpoint, successor);
    }

    @Override
    protected void messageLogic(StatusAndSessionIDMessage message, User user) {
        ErrorResponse error = new ErrorResponse();
        error.status = "WRONG_STATUS";
        error.description = "The status was wrong, please checkLogin the API";
        homeEndpoint.send(error);
    }
}
