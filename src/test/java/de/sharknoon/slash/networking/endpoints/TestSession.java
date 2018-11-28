package de.sharknoon.slash.networking.endpoints;

import javax.websocket.*;
import javax.websocket.MessageHandler.*;
import javax.websocket.RemoteEndpoint.*;
import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class TestSession implements Session {
    
    private final Consumer<String> resultConsumer;
    private final HashMap<String, Object> userProperties = new HashMap<>();
    
    public TestSession(Consumer<String> resultConsumer) {
        this.resultConsumer = resultConsumer;
    }
    
    @Override
    public WebSocketContainer getContainer() {
        return null;
    }
    
    @Override
    public void addMessageHandler(MessageHandler handler) throws IllegalStateException {
    
    }
    
    @Override
    public <T> void addMessageHandler(Class<T> clazz, Whole<T> handler) {
    
    }
    
    @Override
    public <T> void addMessageHandler(Class<T> clazz, Partial<T> handler) {
    
    }
    
    @Override
    public Set<MessageHandler> getMessageHandlers() {
        return null;
    }
    
    @Override
    public void removeMessageHandler(MessageHandler handler) {
    
    }
    
    @Override
    public String getProtocolVersion() {
        return null;
    }
    
    @Override
    public String getNegotiatedSubprotocol() {
        return null;
    }
    
    @Override
    public List<Extension> getNegotiatedExtensions() {
        return null;
    }
    
    @Override
    public boolean isSecure() {
        return false;
    }
    
    private boolean isClosed = false;
    
    @Override
    public long getMaxIdleTimeout() {
        return 0;
    }
    
    @Override
    public void setMaxIdleTimeout(long milliseconds) {
    
    }
    
    @Override
    public void setMaxBinaryMessageBufferSize(int length) {
    
    }
    
    @Override
    public int getMaxBinaryMessageBufferSize() {
        return 0;
    }
    
    @Override
    public void setMaxTextMessageBufferSize(int length) {
    
    }
    
    @Override
    public int getMaxTextMessageBufferSize() {
        return 0;
    }
    
    @Override
    public Async getAsyncRemote() {
        return new Async() {
            @Override
            public long getSendTimeout() {
                return 0;
            }
            
            @Override
            public void setSendTimeout(long timeoutmillis) {
            
            }
            
            @Override
            public void sendText(String text, SendHandler handler) {
            
            }
            
            @Override
            public Future<Void> sendText(String text) {
                resultConsumer.accept(text);
                return null;
            }
            
            @Override
            public Future<Void> sendBinary(ByteBuffer data) {
                return null;
            }
            
            @Override
            public void sendBinary(ByteBuffer data, SendHandler handler) {
            
            }
            
            @Override
            public Future<Void> sendObject(Object data) {
                return null;
            }
            
            @Override
            public void sendObject(Object data, SendHandler handler) {
            
            }
            
            @Override
            public void setBatchingAllowed(boolean allowed) {
            
            }
            
            @Override
            public boolean getBatchingAllowed() {
                return false;
            }
            
            @Override
            public void flushBatch() {
            
            }
            
            @Override
            public void sendPing(ByteBuffer applicationData) throws IllegalArgumentException {
            
            }
            
            @Override
            public void sendPong(ByteBuffer applicationData) throws IllegalArgumentException {
            
            }
        };
    }
    
    @Override
    public boolean isOpen() {
        return !isClosed;
    }
    
    @Override
    public Basic getBasicRemote() {
        return new Basic() {
            @Override
            public void sendText(String text) {
                resultConsumer.accept(text);
            }
        
            @Override
            public void sendBinary(ByteBuffer data) {
            
            }
        
            @Override
            public void sendText(String partialMessage, boolean isLast) {
            
            }
        
            @Override
            public void sendBinary(ByteBuffer partialByte, boolean isLast) {
            
            }
        
            @Override
            public OutputStream getSendStream() {
                return null;
            }
        
            @Override
            public Writer getSendWriter() {
                return null;
            }
        
            @Override
            public void sendObject(Object data) {
            
            }
        
            @Override
            public void setBatchingAllowed(boolean allowed) {
            
            }
        
            @Override
            public boolean getBatchingAllowed() {
                return false;
            }
        
            @Override
            public void flushBatch() {
            
            }
        
            @Override
            public void sendPing(ByteBuffer applicationData) throws IllegalArgumentException {
            
            }
        
            @Override
            public void sendPong(ByteBuffer applicationData) throws IllegalArgumentException {
            
            }
        };
    }
    
    @Override
    public String getId() {
        return null;
    }
    
    @Override
    public void close() {
        isClosed = true;
    }
    
    @Override
    public void close(CloseReason closeReason) {
        isClosed = true;
    }
    
    @Override
    public URI getRequestURI() {
        return null;
    }
    
    @Override
    public Map<String, List<String>> getRequestParameterMap() {
        return null;
    }
    
    @Override
    public String getQueryString() {
        return null;
    }
    
    @Override
    public Map<String, String> getPathParameters() {
        return null;
    }
    
    
    @Override
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }
    
    @Override
    public Principal getUserPrincipal() {
        return null;
    }
    
    @Override
    public Set<Session> getOpenSessions() {
        return null;
    }
}
