package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.networking.endpoints.home.Status;

import java.util.Objects;

public class StatusAndSessionIDMessage {

    @Expose
    private String sessionid = "";
    @Expose
    private Status status = Status.NONE;

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

    public void setStatus(Status status) {
        if (status != null) {
            this.status = status;
        }
    }
}
