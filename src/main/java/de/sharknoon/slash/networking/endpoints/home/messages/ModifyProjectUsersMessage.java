package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;

import java.util.Objects;
import java.util.Set;

public class ModifyProjectUsersMessage extends StatusAndSessionIDMessage {

    @Expose
    @SerializedName(value = "projectID", alternate = {"projectid", "projectId"})
    private String projectID = "";

    @Expose
    private Set<String> users = Set.of();

    @Expose
    private boolean addUsers = true;

    public ModifyProjectUsersMessage() {
        super(Status.MODIFY_PROJECT_USERS);
    }

    public String getProjectID() {
        return Objects.requireNonNullElse(projectID, "");
    }

    public void setProjectID(String projectID) {
        if (projectID != null) {
            this.projectID = projectID;
        }
    }

    public Set<String> getUsers() {
        return Objects.requireNonNullElse(users, Set.of());
    }

    public void setUsers(Set<String> users) {
        if (users != null) {
            this.users = users;
        }
    }

    public boolean isAddUsers() {
        return addUsers;
    }

    public void setAddUsers(boolean addUsers) {
        this.addUsers = addUsers;
    }
}
