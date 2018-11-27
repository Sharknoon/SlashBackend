package de.sharknoon.slash.networking;

import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.login.LoginEndpoint;
import de.sharknoon.slash.networking.endpoints.register.RegisterEndpoint;
import org.bson.types.ObjectId;

import javax.websocket.Session;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Here are all LoginSessions of users, who are successfully logged in
 */
public class LoginSessions {
    //SessionID->loginSession
    private static final Map<String, LoginSession> LOGGED_IN_SESSIONS = new HashMap<>();
    
    public static void addSession(User user, String sessionID, Class<? extends Endpoint> endpoint, Session session) {
        if (LOGGED_IN_SESSIONS.containsKey(sessionID)) {
            LOGGED_IN_SESSIONS.get(sessionID).setSession(endpoint, session);
        } else {
            LoginSession ls = new LoginSession(user);
            ls.setSession(endpoint, session);
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
                .parallelStream()
                .filter(ls -> user.id.equals(ls.user.id))
                .map(ls -> ls.getSession(endpoint))
                .filter(Objects::nonNull)
                .findAny();
    }
    
    public static Set<Session> getSessions(Class<? extends Endpoint> endpoint, Collection<User> users) {
        Set<ObjectId> ids = users.stream().map(u -> u.id).collect(Collectors.toSet());
        return LOGGED_IN_SESSIONS.values()
                .parallelStream()
                .filter(ls -> ids.contains(ls.user.id))
                .map(ls -> ls.getSession(endpoint))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    
    private static class LoginSession {
        private Session loginSession = null;
        private Session registerSession = null;
        private Session homeSession = null;
        private final User user;
    
        LoginSession(User user) {
            this.user = user;
        }
    
        Session getSession(Class<? extends Endpoint> endpoint) {
            if (endpoint == HomeEndpoint.class) {
                return homeSession;
            } else if (endpoint == LoginEndpoint.class) {
                return loginSession;
            } else if (endpoint == RegisterEndpoint.class) {
                return registerSession;
            }
            return null;
        }
    
        void setSession(Class<? extends Endpoint> endpoint, Session session) {
            if (endpoint == HomeEndpoint.class) {
                homeSession = session;
            } else if (endpoint == LoginEndpoint.class) {
                loginSession = session;
            } else if (endpoint == RegisterEndpoint.class) {
                registerSession = session;
            }
        }

    }
    
}
