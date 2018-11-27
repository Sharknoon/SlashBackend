package de.sharknoon.slash.networking.endpoints.home.Messages;

import com.google.gson.annotations.Expose;

public class GetProjectMessage extends StatusAndSessionIDMessage {
    
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
