package de.sharknoon.slash.networking.endpoints;

import com.google.gson.*;

import javax.websocket.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.*;

public abstract class Endpoint<M> {
    
    
    private static final Gson GSON = new Gson();
    private static final Set<Endpoint> ENDPOINTS = new CopyOnWriteArraySet<>();
    private final Class<M> messageClass;
    private final Class<? extends Endpoint> endpointClass;
    
    public Endpoint(Class<M> messageClass) {
        this.messageClass = messageClass;
        this.endpointClass = getClass();
    }
    
    @OnOpen
    public void onOpen(Session session) {
        Logger.getGlobal().log(Level.INFO, session.getId() + " connected");
        ENDPOINTS.add(this);
        
        session.getAsyncRemote().sendText(
                OpeningMessage.getOpeningMessage(endpointClass)
        );
    }
    
    @OnMessage
    public final void onMessage(Session session, String message) {
        Logger.getGlobal().log(Level.INFO, session.getId() + ": " + message);
        try {
            M messageObject = GSON.fromJson(message, messageClass);
            onMessage(
                    session,
                    messageObject
            );
        } catch (JsonSyntaxException je) {
            onError(session, "JSON not well formattet");
        } catch (Exception e) {
            onError(session, e);
        }
    }
    
    //To be implemented
    protected abstract void onMessage(Session session, M message);
    
    @OnClose
    public void onClose(Session session) {
        Logger.getGlobal().log(Level.INFO, session.getId() + " disconnected");
        ENDPOINTS.remove(this);
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        Logger.getGlobal().log(Level.WARNING, session.getId() + " has an Error", throwable);
        
        session.getAsyncRemote().sendText(
                ErrorMessage.getErrorMessage(throwable)
        );
    }
    
    private void onError(Session session, String errorMessage) {
        Logger.getGlobal().log(Level.WARNING, session.getId() + " has an Error: " + errorMessage);
        
        session.getAsyncRemote().sendText(
                ErrorMessage.getErrorMessage(errorMessage)
        );
    }
    
    private static class OpeningMessage {
        
        private static final String JSON = "{\"status\":\"OK\",\"message\":\"Connected to $\"}";
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
            String errorMessage = t.getLocalizedMessage();
            errorMessage = JSON.replace("$", errorMessage);
            JSONS_THROWABLES.put(t, errorMessage);
            return errorMessage;
        }
    }
    
}
