package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.networking.endpoints.home.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddProjectMessage extends StatusAndSessionIDMessage {

    @Expose
    private String projectName = "";
    @Expose
    private String projectDescription = "";
    @Expose
    private List<String> projectMembers = new ArrayList<>();

    public AddProjectMessage() {
        setStatus(Status.ADD_PROJECT);
    }


    public String getProjectName() {
        return Objects.requireNonNullElse(projectName, "");
    }

    public void setProjectName(String projectName) {
        if (projectName != null) {
            this.projectName = projectName;
        }
    }

    public String getProjectDescription() {
        return Objects.requireNonNullElse(projectDescription, "");
    }

    public void setProjectDescription(String projectDescription) {
        if (projectDescription != null) {
            this.projectDescription = projectDescription;
        }
    }

    public List<String> getProjectMembers() {
        return Objects.requireNonNullElse(projectMembers, List.of());
    }

    public void setProjectMembers(List<String> projectMembers) {
        if (projectMembers != null) {
            this.projectMembers = new ArrayList<>(projectMembers);
        }
    }
}
