package de.sharknoon.slash.networking.endpoints.home;

public class HomeMessage {
    
    String sessionid = "";
    
    public String getSessionid() {
        return sessionid;
    }
    
    public void setSessionid(String sessionid) {
        if (sessionid != null) {
            this.sessionid = sessionid;
        }
    }
}
