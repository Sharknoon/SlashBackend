package de.sharknoon.slash.networking.endpoints;

import com.google.gson.*;
import de.sharknoon.slash.networking.utils.*;
import org.bson.types.ObjectId;

import javax.websocket.*;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.*;

public abstract class Endpoint<M> {
    
    //Gson to convert JSON
    private static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeConverter())
            .registerTypeAdapter(ObjectId.class, new ObjectIdConverter())
            .excludeFieldsWithModifiers(Modifier.STATIC)
            .create();
    //The class of the Messages e.g. RegisterMessage or LoginMessage
    private final Class<M> messageClass;
    //The type of the extending class of this class e.g. LoginEndpoint or RegisterEndpoint
    private final Class<? extends Endpoint> endpointClass;
    
    protected static String toJSON(Object o) {
        try {
            return GSON.toJson(o);
        } catch (Exception e) {
            return "{\"status\":\"ERROR\"," +
                    "\"message\":\"An unexpected error occurred, please try again later\"}";
        }
    }
    
    @SuppressWarnings("WeakerAccess")
    public Endpoint(Class<M> messageClass) {
        this.messageClass = messageClass;
        this.endpointClass = getClass();
    }
    
    private Session session;
    
    @OnOpen
    public void onOpen(Session session) {
        Logger.getGlobal().log(Level.INFO, session.getId() + " connected");
        this.session = session;
        session.getAsyncRemote().sendText(
                OpeningMessage.getOpeningMessage(endpointClass)
        );
    }
    
    //To be implemented
    protected abstract void onMessage(Session session, M message);
    
    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        Logger.getGlobal().log(Level.INFO, session.getId() + " disconnected");
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        Logger.getGlobal().log(Level.SEVERE, session.getId() + " has an Error", throwable);
        
        session.getAsyncRemote().sendText(
                ErrorMessage.getErrorMessage(throwable)
        );
    }
    
    private void onError(Session session, String errorMessage) {
        Logger.getGlobal().log(Level.SEVERE, session.getId() + " has an Error: " + errorMessage);
        
        session.getAsyncRemote().sendText(
                ErrorMessage.getErrorMessage(errorMessage)
        );
    }
    
    @OnMessage
    public final void onMessage(Session session, String message) {
        Logger.getGlobal().log(Level.INFO, session.getId() + ": " + message);
        this.session = session;
        try {
            M messageObject = GSON.fromJson(message, messageClass);
            onMessage(
                    session,
                    messageObject
            );
        } catch (JsonSyntaxException je) {
            onError(session, "JSON not well formatted");
        } catch (Exception e) {
            onError(session, "Internal server error occurred");
        }
    }
    
    protected void send(String json) {
        if (session != null) {
            session.getAsyncRemote().sendText(json);
        }
    }
    
    protected void send(Object o) {
        send(toJSON(o));
    }
    
    private static class OpeningMessage {
    
        private static final String JSON = "{\"status\":\"CONNECTED\",\"message\":\"Connected to $\"}";
        private static final Map<Class<?>, String> JSONS = new HashMap<>();
        
        static String getOpeningMessage(Class<?> messageClass) {
            if (JSONS.containsKey(messageClass)) {
                return JSONS.get(messageClass);
            }
            String className = messageClass.getSimpleName();
            className = JSON.replace("$", className);
            JSONS.put(messageClass, className);
            return className;
        }
    }
    
    private static class ErrorMessage {
        private static final String JSON = "{\"status\":\"ERROR\",\"message\":\"$\"}";
        private static final Map<Throwable, String> JSONS_THROWABLES = new HashMap<>();
        private static final Map<String, String> JSON_STRINGS = new HashMap<>();
        
        static String getErrorMessage(String message) {
            if (JSON_STRINGS.containsKey(message)) {
                return JSON_STRINGS.get(message);
            }
            String errorMessage = JSON.replace("$", message);
            JSON_STRINGS.put(message, errorMessage);
            return errorMessage;
        }
        
        static String getErrorMessage(Throwable t) {
            if (JSONS_THROWABLES.containsKey(t)) {
                return JSONS_THROWABLES.get(t);
            }
            String errorMessage = t.getClass().getSimpleName() + " " + t.getLocalizedMessage();
            errorMessage = JSON.replace("$", errorMessage);
            JSONS_THROWABLES.put(t, errorMessage);
            return errorMessage;
        }
    }
    
}
