package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;

import java.util.Objects;

public class ModifyProjectOwnerMessage extends StatusAndSessionIDMessage {

    @Expose
    @SerializedName(value = "projectID", alternate = {"projectid", "projectId"})
    private String projectID = "";

    @Expose
    private String projectOwner = "";

    public ModifyProjectOwnerMessage() {
        super(Status.MODIFY_PROJECT_OWNER);
    }

    public String getProjectID() {
        return Objects.requireNonNullElse(projectID, "");
    }

    public void setProjectID(String projectID) {
        if (projectID != null) {
            this.projectID = projectID;
        }
    }

    public String getProjectOwner() {
        return Objects.requireNonNullElse(projectOwner, "");
    }

    public void setProjectOwner(String projectOwner) {
        if (projectOwner != null) {
            this.projectOwner = projectOwner;
        }
    }

}
