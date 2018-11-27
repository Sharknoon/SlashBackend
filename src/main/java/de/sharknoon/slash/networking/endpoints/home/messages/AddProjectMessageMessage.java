package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.networking.endpoints.home.Status;

public class AddProjectMessageMessage extends AddMessageMessage {

    public AddProjectMessageMessage() {
        setStatus(Status.ADD_PROJECT_MESSAGE);
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
