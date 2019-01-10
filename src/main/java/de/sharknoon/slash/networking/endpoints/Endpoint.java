package de.sharknoon.slash.networking.endpoints;

import javax.websocket.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public abstract class Endpoint implements Sendable {

    //The type of the extending class of this class e.g. LoginEndpoint or RegisterEndpoint
    private final Class<? extends Endpoint> endpointClass;
    //The current Session for easy send(...) calls
    private Session session;
    private String lastTextMessage = "";

    static {
        FileHandler fh;
        try {
            // This block configure the logger with handler and formatter
            Path logFilePath = Paths.get(System.getProperty("user.home"), ".slash", "slash-errors.log");
            Files.createDirectories(logFilePath.getParent());
            Files.deleteIfExists(logFilePath);
            fh = new FileHandler(logFilePath.toString());
            fh.setLevel(Level.WARNING);
            Logger.getGlobal().addHandler(fh);
            fh.setFormatter(new SimpleFormatter());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Endpoint() {
        this.endpointClass = getClass();
    }

    @Override
    public Session getSession() {
        return session;
    }

    @OnOpen
    public void onOpen(Session session) {
        Logger.getGlobal().info(session.getId() + " connected");
        this.session = session;
        session.getAsyncRemote().sendText(
                OpeningMessage.getOpeningMessage(endpointClass)
        );
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        Logger.getGlobal().info(session.toString() + "disconnected because " + closeReason.toString());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        Logger.getGlobal().log(Level.SEVERE, "Session: " + session.toString() + " Error: " + throwable.getClass().getSimpleName(), throwable);

        session.getAsyncRemote().sendText(
                ErrorMessage.getErrorMessage(throwable)
        );
    }

    private void onError(Session session, String errorMessage) {
        Logger.getGlobal().severe("Session: " + session.toString() + " Error: " + errorMessage);

        session.getAsyncRemote().sendText(
                ErrorMessage.getErrorMessage(errorMessage)
        );
    }

    protected void handleMessage(Runnable message) {
        handleMessage(null, message);
    }

    protected void handleMessage(String s, Runnable message) {
        if (s != null) {
            lastTextMessage = s;
        }
        try {
            message.run();
        } catch (Exception e) {
            onError(session, e);
        }
    }

    public String getLastTextMessage() {
        return lastTextMessage;
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
