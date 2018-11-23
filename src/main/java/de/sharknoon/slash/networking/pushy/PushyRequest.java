package de.sharknoon.slash.networking.pushy;

import com.google.gson.annotations.Expose;

class PushyPushRequest {
    @Expose
    private final Object to;
    @Expose
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
