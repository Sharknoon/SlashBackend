package de.sharknoon.slash.networking.endpoints;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class StatusAndSessionIDMessage {

    @Expose
    @SerializedName(value = "sessionid", alternate = {"sessionID", "sessionId"})
    private String sessionid = "";
    @Expose
    private final Status status;

    public StatusAndSessionIDMessage(Status status) {
        this.status = status;
    }
    
    public String getSessionid() {
        return Objects.requireNonNullElse(sessionid, "");
    }

    public void setSessionid(String sessionid) {
        if (sessionid != null) {
            this.sessionid = sessionid;
        }
    }

    public Status getStatus() {
        return Objects.requireNonNullElse(status, Status.NONE);
    }

}
