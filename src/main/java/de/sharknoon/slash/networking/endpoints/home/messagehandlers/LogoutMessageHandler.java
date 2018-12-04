package de.sharknoon.slash.networking.endpoints.home.messagehandlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.LoginSessions;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.Status;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.ErrorResponse;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.LogoutResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.StatusAndSessionIDMessage;

import java.util.Optional;

public class LogoutMessageHandler extends HomeEndpointMessageHandler {
    public LogoutMessageHandler(HomeEndpoint homeEndpoint, HomeEndpointMessageHandler successor) {
        super(Status.LOGOUT, homeEndpoint, successor);
    }

    @Override
    public void messageLogic(StatusAndSessionIDMessage message, User user) {
        final String sessionID = message.getSessionid();
        Optional<String> optionalDeviceID = LoginSessions.getDeviceID(sessionID);
        if (optionalDeviceID.isEmpty()) {
            //to send a internal server error, this should never happen
            ErrorResponse error = new ErrorResponse();
            error.status = "DEVICE_ID_NOT_FOUND";
            error.description = "The device-id of this session could not be not found";
            homeEndpoint.send(error);
        } else {
            String deviceID = optionalDeviceID.get();
            DB.removeSessionID(user, sessionID);
            DB.removeDeviceID(user, deviceID);
            homeEndpoint.sendSync(new LogoutResponse());
            LoginSessions.removeSession(sessionID);
        }
    }
}