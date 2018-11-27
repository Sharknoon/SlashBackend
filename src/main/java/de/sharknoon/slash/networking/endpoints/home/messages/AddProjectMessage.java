package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.networking.endpoints.home.Status;

public class AddProjectMessage extends StatusAndSessionIDMessage {

    public AddProjectMessage() {
        setStatus(Status.ADD_PROJECT);
    }

    @Expose
    private String projectName = "";
    @Expose
    private String projectDescription = "";

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        if (projectName != null) {
            this.projectName = projectName;
        }
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        if (projectDescription != null) {
            this.projectDescription = projectDescription;
        }
    }
}
