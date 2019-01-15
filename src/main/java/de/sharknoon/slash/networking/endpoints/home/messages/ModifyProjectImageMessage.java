package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;

import java.util.Objects;

public class ModifyProjectImageMessage extends StatusAndSessionIDMessage {

    @Expose
    @SerializedName(value = "projectID", alternate = {"projectid", "projectId"})
    private String projectID = "";

    @Expose
    private boolean remove = false;

    public ModifyProjectImageMessage() {
        super(Status.MODIFY_PROJECT_IMAGE);
    }

    public String getProjectID() {
        return Objects.requireNonNullElse(projectID, "");
    }

    public void setProjectID(String projectID) {
        if (projectID != null) {
            this.projectID = projectID;
        }
    }

    public boolean isRemoved() {
        return remove;
    }

    public void setRemoved(boolean removed) {
        this.remove = removed;
    }

}
