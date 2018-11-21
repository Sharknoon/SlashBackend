package de.sharknoon.slash.networking;

import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Endpoint;

import javax.websocket.Session;
import java.util.*;

/**
 * Here are all LoginSessions of users, who are successfully logged in
 */
public class LoginSessions {
    
    private static final Map<String, LoginSession> LOGGED_IN_SESSIONS = new HashMap<>();
    
    public static void addSession(User user, String sessionID, Class<? extends Endpoint> endpoint, Session session) {
        if (LOGGED_IN_SESSIONS.containsKey(sessionID)) {
            LOGGED_IN_SESSIONS.get(sessionID).session.put(endpoint, session);
        } else {
            LoginSession ls = new LoginSession(endpoint, session, user);
            LOGGED_IN_SESSIONS.put(sessionID, ls);
        }
    }
    
    public static void removeSession(String sessionID) {
        LOGGED_IN_SESSIONS.remove(sessionID);
    }
    
    /**
     * Checks and returns the user for the specified session ID
     *
     * @param sessionID The sessionID from the client
     * @return The user if the client has successfully logged in, an empty optional otherwise
     */
    public static Optional<User> getUser(String sessionID) {
        LoginSession loginSession = LOGGED_IN_SESSIONS.get(sessionID);
        if (loginSession == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(loginSession.user);
    }
    
    public static Optional<Session> getSession(Class<? extends Endpoint> endpoint, User user) {
        return LOGGED_IN_SESSIONS.values()
                .stream()
                .filter(ls -> user.equals(ls.user))
                .map(ls -> ls.session)
                .filter(session -> session.containsKey(endpoint))
                .map(session -> session.get(endpoint))
                .findAny();
    }
    
    private static class LoginSession {
        private final Map<Class<? extends Endpoint>, Session> session = new HashMap<>();
        private final User user;
        
        LoginSession(Class<? extends Endpoint> endpoint, Session session, User user) {
            this.session.put(endpoint, session);
            this.user = user;
        }
    }
    
}
