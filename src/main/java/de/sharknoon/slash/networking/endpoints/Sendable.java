package de.sharknoon.slash.networking.endpoints;

import javax.websocket.*;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public interface Sendable {
    
    String serialize(Object object);
    
    Session getSession();
    
    default CompletableFuture<Void> send(byte[] binary) {
        return sendTo(getSession(), binary);
    }
    
    default CompletableFuture<Void> send(String text) {
        return sendTo(getSession(), text);
    }
    
    default CompletableFuture<Void> send(Object object) {
        return sendTo(getSession(), serialize(object));
    }
    
    default CompletableFuture<Void> sendTo(Session session, byte[] binary) {
        return sendTo(session, true, binary, false);
    }
    
    default CompletableFuture<Void> sendTo(Session session, String text) {
        return sendTo(session, true, text, true);
    }
    
    default CompletableFuture<Void> sendTo(Session session, Object object) {
        return sendTo(session, true, serialize(object), true);
    }
    
    
    default void sendSync(byte[] binary) {
        sendSyncTo(getSession(), binary);
    }
    
    default void sendSync(String text) {
        sendSyncTo(getSession(), text);
    }
    
    default void sendSync(Object object) {
        sendSyncTo(getSession(), serialize(object));
    }
    
    
    default void sendSyncTo(Session session, byte[] binary) {
        sendTo(session, false, binary, false);
    }
    
    default void sendSyncTo(Session session, String text) {
        sendTo(session, false, text, true);
    }
    
    default void sendSyncTo(Session session, Object object) {
        sendTo(session, false, serialize(object), true);
    }
    
    
    private CompletableFuture<Void> sendTo(Session session, boolean async, Object object, boolean text) {
        try {
            CompletableFuture<Void> cf;
            if (async) {
                cf = new CompletableFuture<>();
                if (text) {
                    session.getAsyncRemote().sendText((String) object, constructSendHandler(cf));
                } else {
                    session.getAsyncRemote().sendBinary(ByteBuffer.wrap((byte[]) object), constructSendHandler(cf));
                }
            } else {
                cf = CompletableFuture.completedFuture(null);
                if (text) {
                    session.getBasicRemote().sendText((String) object);
                } else {
                    session.getBasicRemote().sendBinary(ByteBuffer.wrap((byte[]) object));
                }
            }
            return cf;
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
    
    
    private SendHandler constructSendHandler(CompletableFuture<Void> cf) {
        return sendResult -> {
            if (sendResult.isOK()) {
                cf.complete(null);
            } else {
                cf.completeExceptionally(sendResult.getException());
            }
        };
    }
    
}
