package de.sharknoon.slash.networking.apis.pushy;

import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.database.models.message.Message;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Pushy {

    public static void sendPush(PushStatus pushStatus, String referenceID, String from, User... to) {
        sendPush(pushStatus, referenceID, from, Arrays.asList(to));
    }

    public static void sendPush(PushStatus pushStatus, String referenceID, String from, Collection<User> to) {
        Optional<List<String>> toListOptional = checkReceiversList(to);
        if (toListOptional.isEmpty()) {
            return;
        }
        List<String> toList = toListOptional.get();

        Map<String, Object> payload = createPayload(pushStatus.name(), referenceID, null, from, null);

        sendMessagePush(payload, toList);
    }

    public static void sendMessagePush(PushStatus status, String referenceID, Message message, String from, User... to) {
        sendMessagePush(status, referenceID, message, from, Arrays.asList(to));
    }

    /**
     * @param status      The status sayswhich type of notification this is, e.g. new chat message or new project message
     * @param referenceID This is a reference to a chat, a project or something else, dependent on the status
     * @param message     The message itself
     * @param from        The sender
     * @param to          The receiver
     */
    public static void sendMessagePush(PushStatus status, String referenceID, Message message, String from, Collection<User> to) {
        Optional<List<String>> toListOptional = checkReceiversList(to);
        if (toListOptional.isEmpty()) {
            return;
        }
        List<String> toList = toListOptional.get();

        // Set payload (any object, it will be serialized to JSON)
        Object content = "content";
        switch (message.type) {
            case TEXT:
                content = message.content;
                break;
            case EMOTION:
                content = Map.of(
                        "category", message.emotion.name(),
                        "subject", message.subject,
                        "message", message.content
                );
                break;
            case IMAGE:
                content = message.image.toString();
                break;
            case NONE:
                Logger.getGlobal().warning("Message for push notification has a NONE type " + message.toString());
                return;
        }

        Map<String, Object> payload = createPayload(status.name(), referenceID, message.type.name(), from, content);

        sendMessagePush(payload, toList);
    }

    private static void sendMessagePush(Map<String, Object> content, Collection<String> to) {
        //If we dont have any receiver, abort
        if (to.isEmpty()) {
            return;
        }
        // Prepare the push request
        PushyPushRequest push = new PushyPushRequest(content, to);

        try {
            // Try sending the push notification
            PushyAPI.sendPushAsync(push, t -> Logger.getGlobal().severe("Could not send push notification " + t.toString()));
        } catch (Exception exc) {
            // Error, print to console
            Logger.getGlobal().severe("Could not send push notification " + exc.toString());
        }
    }

    private static Optional<List<String>> checkReceiversList(Collection<User> to) {
        //If we dont have any receiver, abort
        if (to.isEmpty()) {
            return Optional.empty();
        }

        // Prepare list of target device tokens
        List<String> toList = to
                .parallelStream()
                .flatMap(u -> u.ids.stream())
                .map(l -> l.deviceID)
                .collect(Collectors.toList());

        //If we dont have any receiver, abort
        if (toList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toList);
    }

    private static Map<String, Object> createPayload(String status, String id, String type, String from, Object content) {
        Map<String, Object> payload = new HashMap<>();
        if (status != null) {
            payload.put("status", status);
        }
        if (id != null) {
            payload.put("id", id);
        }
        if (type != null) {
            payload.put("type", type);
        }
        if (from != null) {
            payload.put("from", from);
        }
        if (content != null) {
            payload.put("content", content);
        }
        return payload;
    }

}
