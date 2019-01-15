package de.sharknoon.slash.networking.endpoints.home.handlers;

import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.handlers.response.ErrorResponse;

public class NoneStatusHandler extends HomeEndpointHandler {

    public NoneStatusHandler(HomeEndpoint homeEndpoint) {
        super(Status.NONE, homeEndpoint);
    }

    public NoneStatusHandler(HomeEndpoint homeEndpoint, HomeEndpointHandler successor) {
        super(Status.NONE, homeEndpoint, successor);
    }

    @Override
    protected void messageLogic(StatusAndSessionIDMessage message, User user) {
        ErrorResponse error = new ErrorResponse();
        error.status = "WRONG_STATUS";
        error.description = "The status was wrong, please checkLogin the API";
        homeEndpoint.send(error);
    }
}
