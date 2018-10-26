package de.sharknoon.endpoints;

import com.google.gson.Gson;

import javax.websocket.*;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.*;

public abstract class Endpoint<M> {
    
    
    protected static final Gson GSON = new Gson();
    private static final Set<Endpoint> ENDPOINTS = new CopyOnWriteArraySet<>();
    private final Class<M> messageClass;
    
    public Endpoint(Class<M> messageClass) {
        this.messageClass = messageClass;
    }
    
    @OnOpen
    public void onOpen(Session session) {
        Logger.getGlobal().log(Level.INFO, session.getId() + " connected");
        ENDPOINTS.add(this);
        
        session.getAsyncRemote().sendText(
                GSON.toJson(
                        new OpeningMessage()
                )
        );
    }
    
    @OnMessage
    public final void onMessage(Session session, String message) {
        Logger.getGlobal().log(Level.INFO, session.getId() + ": " + message);
        onMessage(
                session,
                GSON.fromJson(message, messageClass)
        );
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
                GSON.toJson(
                        new ErrorMessage(throwable.getMessage())
                )
        );
    }
    
    private class OpeningMessage {
        String status = "OK";
        String message = "Connected to " + Endpoint.this.getClass().getSimpleName();
    }
    
    private class ErrorMessage {
        String status = "ERROR";
        String message;
        
        ErrorMessage(String message) {
            this.message = message;
        }
    }
    
}
