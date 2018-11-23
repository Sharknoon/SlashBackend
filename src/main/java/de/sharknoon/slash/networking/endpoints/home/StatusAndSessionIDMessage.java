package de.sharknoon.slash.networking.endpoints.home;

import com.google.gson.annotations.Expose;

class StatusAndSessionIDMessage {
    
    @Expose
    private String sessionid = "";
    @Expose
    private Status status = Status.NONE;
    
    String getSessionid() {
        return sessionid;
    }
    
    void setSessionid(String sessionid) {
        if (sessionid != null) {
            this.sessionid = sessionid;
        }
    }
    
    Status getStatus() {
        return status;
    }
    
    void setStatus(Status status) {
        if (status != null) {
            this.status = status;
        }
    }
}
