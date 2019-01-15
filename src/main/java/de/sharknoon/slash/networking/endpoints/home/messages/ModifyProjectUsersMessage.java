package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;

import java.util.Objects;

public class ModifyProjectUsersMessage extends StatusAndSessionIDMessage {

    @Expose
    @SerializedName(value = "projectID", alternate = {"projectid", "projectId"})
    private String projectID = "";

    @Expose
    @SerializedName(value = "userID", alternate = {"userid", "userId"})
    private String userID = "";

    @Expose
    private boolean addUser = true;

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

    public String getUserID() {
        return Objects.requireNonNullElse(userID, "");
    }

    public void setUserID(String userID) {
        if (userID != null) {
            this.userID = userID;
        }
    }

    public boolean isAddUser() {
        return addUser;
    }

    public void setAddUser(boolean addUser) {
        this.addUser = addUser;
    }
}
