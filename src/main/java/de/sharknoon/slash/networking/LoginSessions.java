package de.sharknoon.slash.networking;

import de.sharknoon.slash.database.models.User;

import java.util.*;

/**
 * Here are all LoginSessions of users, who are successfully logged in
 */
public class LoginSessions {
    
    private static Map<User, Set<String>> LOGGED_IN_USERS = new HashMap<>();
    private static Map<String, User> LOGGED_IN_SESSIONS = new HashMap<>();
    
    public static void addSession(User user, String sessionID) {
        if (!LOGGED_IN_USERS.containsKey(user)) {
            LOGGED_IN_USERS.put(user, new HashSet<>());
        }
        LOGGED_IN_USERS.get(user).add(sessionID);
        LOGGED_IN_SESSIONS.put(sessionID, user);
    }
    
    public static void removeSession(String sessionID) {
        User removed = LOGGED_IN_SESSIONS.remove(sessionID);
        if (removed != null) {
            Set<String> sessions = LOGGED_IN_USERS.get(removed);
            if (sessions != null) {
                sessions.remove(sessionID);
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
        return Optional.ofNullable(LOGGED_IN_SESSIONS.get(sessionID));
    }
    
}
