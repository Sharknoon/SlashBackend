package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.networking.endpoints.home.Status;

public class GetProjectMessage extends StatusAndSessionIDMessage {

    public GetProjectMessage() {
        setStatus(Status.GET_PROJECT);
    }
    
    @Expose
    private String projectID = "";

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        if (projectID != null) {
            this.projectID = projectID;
        }
    }
    
}
