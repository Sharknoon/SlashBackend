package de.sharknoon.slash.networking.sessions;

import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import org.bson.types.ObjectId;

import javax.websocket.Session;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Here are all LoginSessions of users, who are successfully logged in
 */
public class LoginSessions {
    //SessionID->loginSession
    private static final Map<String, LoginSession> LOGGED_IN_SESSIONS = new HashMap<>();
    
    public static void addSession(User user, String sessionID, String deviceID, Class<? extends Endpoint> endpoint, Session session) {
        if (LOGGED_IN_SESSIONS.containsKey(sessionID)) {
            LOGGED_IN_SESSIONS.get(sessionID).setSession(endpoint, session);
        } else {
            LoginSession ls = new LoginSession(deviceID, user);
            ls.setSession(endpoint, session);
            LOGGED_IN_SESSIONS.put(sessionID, ls);
        }
    }
    
    public static void removeSession(String sessionID) {
        LoginSession remove = LOGGED_IN_SESSIONS.remove(sessionID);
        if (remove != null) {
            try {
                Session homeSession = remove.getHomeSession();
                if (homeSession != null) {
                    homeSession.close();
                }
                Session loginSession = remove.getLoginSession();
                if (loginSession != null) {
                    loginSession.close();
                }
                Session registerSession = remove.getRegisterSession();
                if (registerSession != null) {
                    registerSession.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        return Optional.ofNullable(loginSession.getUser());
    }
    
    public static Optional<String> getDeviceID(String sessionID) {
        LoginSession loginSession = LOGGED_IN_SESSIONS.get(sessionID);
        if (sessionID == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(loginSession.getDeviceID());
    }
    
    public static Optional<Session> getSession(Class<? extends Endpoint> endpoint, User user) {
        return LOGGED_IN_SESSIONS.values()
                .parallelStream()
                .filter(ls -> user.id.equals(ls.getUser().id))
                .map(ls -> ls.getSession(endpoint))
                .peek(s -> System.out.println(Objects.hashCode(s)))
                .filter(Objects::nonNull)
                .findAny();
    }
    
    public static Set<Session> getSessions(Class<? extends Endpoint> endpoint, Collection<User> users) {
        Set<ObjectId> ids = users.stream().map(u -> u.id).collect(Collectors.toSet());
        return LOGGED_IN_SESSIONS.values()
                .parallelStream()
                .filter(ls -> ids.contains(ls.getUser().id))
                .map(ls -> ls.getSession(endpoint))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
    
    public static Set<Map.Entry<Session, User>> getSessionsWithUser(Class<? extends Endpoint> endpoint, Collection<User> users) {
        Set<ObjectId> ids = users.stream().map(u -> u.id).collect(Collectors.toSet());
        return LOGGED_IN_SESSIONS.values()
                .parallelStream()
                .filter(ls -> ids.contains(ls.getUser().id))
                .map(ls -> Map.entry(ls.getSession(endpoint), ls.getUser()))
                .filter(e -> e.getKey() != null)
                .collect(Collectors.toSet());
    }
    
}
