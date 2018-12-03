package de.sharknoon.slash.networking.endpoints.home.messagehandlers;

import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.Status;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.ErrorResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.StatusAndSessionIDMessage;

public class NoStatusMessageHandler extends HomeEndpointMessageHandler {
    public NoStatusMessageHandler(HomeEndpoint homeEndpoint, HomeEndpointMessageHandler successor) {
        super(homeEndpoint, successor);
    }

    @Override
    public void handleMessage(StatusAndSessionIDMessage message, User user) {
        if (Status.NONE != message.getStatus() || message.getStatus() == null) {
            if (successor != null) {
                successor.handleMessage(message, user);
            }
        } else {
            ErrorResponse error = new ErrorResponse();
            error.status = "WRONG_STATUS";
            error.description = "The status was wrong, please check the API";
            homeEndpoint.send(error);
        }
    }
}
