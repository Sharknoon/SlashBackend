package de.sharknoon.slash.networking.endpoints.home;

import com.google.gson.annotations.Expose;

class AddProjectMessageMessage extends AddMessageMessage {
    
    
    @Expose
    private String projectID = "";
    
    String getProjectID() {
        return projectID;
    }
    
    void setProjectID(String projectID) {
        if (projectID != null) {
            this.projectID = projectID;
        }
    }
    
}
