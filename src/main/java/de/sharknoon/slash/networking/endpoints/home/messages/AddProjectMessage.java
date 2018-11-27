package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.networking.endpoints.home.Status;

import java.util.ArrayList;
import java.util.List;

public class AddProjectMessage extends StatusAndSessionIDMessage {

    @Expose
    private String projectName = "";
    @Expose
    private String projectDescription = "";
    @Expose
    private List<String> memberIDs = new ArrayList<>();

    public AddProjectMessage() {
        setStatus(Status.ADD_PROJECT);
    }


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

    public List<String> getMemberIDs() {
        return new ArrayList<>(memberIDs);
    }

    public void setMemberIDs(List<String> memberIDs) {
        this.memberIDs = new ArrayList<>(memberIDs);
    }
}