package de.sharknoon.slash.networking.pushy;

import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.database.models.message.Message;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Pushy {
    
    public static void sendPush(PushStatus status, String chatOrProjectID, Message message, String from, User... to) {
        sendPush(status, chatOrProjectID, message, from, Arrays.asList(to));
    }
    
    public static void sendPush(PushStatus status, String chatOrProjectID, Message message, String from, Collection<User> users) {
        // Prepare list of target device tokens
        List<String> to = users
                .parallelStream()
                .map(u -> u.ids)
                .flatMap(m -> m.keySet().stream())
                .collect(Collectors.toList());
        
        //If we dont have any receiver, abort
        if (to.isEmpty()) {
            return;
        }
        
        // Set payload (any object, it will be serialized to JSON)
        Map<String, Object> payload = new HashMap<>();
        
        payload.put("status", status.name());
        payload.put("id", chatOrProjectID);
        payload.put("type", message.type.name());
        payload.put("from", from);
        switch (message.type) {
            case TEXT:
                payload.put("content", message.content);
                break;
            case EMOTION:
                Map<String, String> emotion = Map.of(
                        "category", message.emotion.name(),
                        "subject", message.subject,
                        "message", message.content
                );
                payload.put("content", emotion);
                break;
            case IMAGE:
                payload.put("content", message.imageUrl.toString());
                break;
            case NONE:
                Logger.getGlobal().warning("Message for push notification has a NONE type " + message.toString());
                return;
        }
        
        // Prepare the push request
        PushyPushRequest push = new PushyPushRequest(payload, to);
        
        try {
            // Try sending the push notification
            PushyAPI.sendPushAsync(push, t -> Logger.getGlobal().severe("Could not send push notification " + t.toString()));
        } catch (Exception exc) {
            // Error, print to console
            Logger.getGlobal().severe("Could not send push notification " + exc.toString());
        }
    }
    
}
