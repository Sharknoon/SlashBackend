package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.networking.endpoints.home.Status;

public class GetUserMessage extends StatusAndSessionIDMessage {

    public GetUserMessage() {
        setStatus(Status.GET_USER);
    }

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
