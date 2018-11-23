package de.sharknoon.slash.networking.pushy;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.User;

import java.util.*;

public class Pushy {
    public static void main(String[] args) {
        Optional<User> user = DB.getUser("Sharknoon");
        if (user.isEmpty()) {
            System.err.println("User Sharknoon not found.");
            return;
        }
        User sharknoon = user.get();
        sendPush(sharknoon);
    }
    
    private static void sendPush(User user) {
        // Prepare list of target device tokens
        Set<String> deviceTokens = user.deviceIDs;
        
        // Convert to String[] array
        String[] to = deviceTokens.toArray(String[]::new);
        
        // Set payload (any object, it will be serialized to JSON)
        Map<String, String> payload = new HashMap<>();
        
        // Add "message" parameter to payload
        payload.put("message", "Hello World!");
        
        // Prepare the push request
        PushyAPI.PushyPushRequest push = new PushyAPI.PushyPushRequest(payload, to);
        
        try {
            // Try sending the push notification
            PushyAPI.sendPushAsync(push, throwable -> System.err.println(throwable.toString()));
        } catch (Exception exc) {
            // Error, print to console
            System.err.println(exc.toString());
        }
    }
}
