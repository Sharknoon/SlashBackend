package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;

public class GetUserMessage extends StatusAndSessionIDMessage {
    
    @Expose
    private String username = "";
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        if (username != null) {
            this.username = username;
        }
    }
}
