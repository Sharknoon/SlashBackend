package de.sharknoon.slash.networking.apis.pushy;

import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.database.models.message.Message;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Pushy {

    public static void sendPush(PushStatus status, String referenceID, String message, String from, User... to) {
        Message m = new Message();
        m.content = message;
        sendPush(status, referenceID, m, from, to);
    }

    public static void sendPush(PushStatus status, String referenceID, Message message, String from, User... to) {
        sendPush(status, referenceID, message, from, Arrays.asList(to));
    }

    /**
     * @param status      The status sayswhich type of notification this is, e.g. new chat message or new project message
     * @param referenceID This is a reference to a chat, a project or something else, dependent on the status
     * @param message     The message itself
     * @param from        The sender
     * @param to          The receiver
     */
    public static void sendPush(PushStatus status, String referenceID, Message message, String from, Collection<User> to) {
        // Prepare list of target device tokens
        List<String> toList = to
                .parallelStream()
                .flatMap(u -> u.ids.stream())
                .map(l -> l.deviceID)
                .collect(Collectors.toList());

        //If we dont have any receiver, abort
        if (toList.isEmpty()) {
            return;
        }

        // Set payload (any object, it will be serialized to JSON)
        Map<String, Object> payload = new HashMap<>();

        payload.put("status", status.name());
        payload.put("id", referenceID);
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
                payload.put("content", message.image.toString());
                break;
            case NONE:
                Logger.getGlobal().warning("Message for push notification has a NONE type " + message.toString());
                return;
        }

        // Prepare the push request
        PushyPushRequest push = new PushyPushRequest(payload, toList);

        try {
            // Try sending the push notification
            PushyAPI.sendPushAsync(push, t -> Logger.getGlobal().severe("Could not send push notification " + t.toString()));
        } catch (Exception exc) {
            // Error, print to console
            Logger.getGlobal().severe("Could not send push notification " + exc.toString());
        }
    }

}
