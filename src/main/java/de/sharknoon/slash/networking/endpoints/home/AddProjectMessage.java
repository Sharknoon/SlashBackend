package de.sharknoon.slash.networking.endpoints.home;

import com.google.gson.annotations.Expose;

class AddProjectMessage extends StatusAndSessionIDMessage {
    
    @Expose
    private String projectName = "";
    @Expose
    private String projectDescription = "";
    
    String getProjectName() {
        return projectName;
    }
    
    void setProjectName(String projectName) {
        if (projectName != null) {
            this.projectName = projectName;
        }
    }
    
    String getProjectDescription() {
        return projectDescription;
    }
    
    void setProjectDescription(String projectDescription) {
        if (projectDescription != null) {
            this.projectDescription = projectDescription;
        }
    }
}
