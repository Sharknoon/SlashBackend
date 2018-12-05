package de.sharknoon.slash.networking.pushy;

import com.google.gson.annotations.Expose;

import java.util.*;

class PushyPushRequest {
    @Expose
    private final List<String> to;
    @Expose
    private final Map<String, Object> data;
    //One Week of time-to-live in seconds
    @Expose
    private final int time_to_live = 60 * 60 * 24 * 7;
    
    PushyPushRequest(Map<String, Object> data, List<String> to) {
        this.to = to;
        this.data = data;
    }
    
    List<String> getTo() {
        return to;
    }
    
    Map<String, Object> getData() {
        return data;
    }
}
