package de.sharknoon.slash.networking.sessions;

import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import de.sharknoon.slash.properties.Properties;

import javax.websocket.Session;
import java.util.Optional;

public class LoginSessionUtils {

    public static Optional<User> checkLogin(String sessionID, Session session, Endpoint endpoint) {
        Optional<User> optionalUser = LoginSessions.isLoggedIn(sessionID, session);
        if (optionalUser.isEmpty()) {
            endpoint.send("{\"status\":\"NO_LOGIN_OR_TOO_MUCH_DEVICES\"," +
                    "\"messageType\":\"You are either not logged in or using more than " +
                    Properties.getUserConfig().maxdevices() + " devices\"}");
        }
        return optionalUser;
    }

}
