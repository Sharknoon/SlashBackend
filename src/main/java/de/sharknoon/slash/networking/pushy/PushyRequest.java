package de.sharknoon.slash.networking.pushy;

class PushyPushRequest {
    private final Object to;
    private final Object data;
    
    PushyPushRequest(Object data, Object to) {
        this.to = to;
        this.data = data;
    }
    
    Object getTo() {
        return to;
    }
    
    Object getData() {
        return data;
    }
}
