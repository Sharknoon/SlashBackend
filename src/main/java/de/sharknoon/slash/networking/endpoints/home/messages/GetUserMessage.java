package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.networking.endpoints.home.Status;

public class GetUserMessage extends StatusAndSessionIDMessage {
    
    @Expose
    private String userID = "";
    
    public GetUserMessage() {
        setStatus(Status.GET_USER);
    }
    
    public String getUserID() {
        return userID;
    }
    
    public void setUserID(String userID) {
        if (userID != null) {
            this.userID = userID;
        }
    }
}
