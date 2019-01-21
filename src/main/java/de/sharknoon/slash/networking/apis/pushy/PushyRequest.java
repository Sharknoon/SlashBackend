package de.sharknoon.slash.networking.apis.pushy;

import com.google.gson.annotations.Expose;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

class PushyPushRequest {
    //One Week of time-to-live in seconds
    @Expose
    private static final long time_to_live = Duration.ofDays(7).toSeconds();
    @Expose
    private final Map<String, Object> data;
    @Expose
    private final Set<String> to;

    PushyPushRequest(Map<String, Object> data, Collection<String> to) {
        this.to = Set.copyOf(to);
        this.data = data;
    }

    Set<String> getTo() {
        return to;
    }

    Map<String, Object> getData() {
        return data;
    }
}
