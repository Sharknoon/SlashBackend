package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import de.sharknoon.slash.networking.endpoints.Status;

import java.util.Objects;

public class AddProjectMessageMessage extends AddMessageMessage {

    public AddProjectMessageMessage() {
        super(Status.ADD_PROJECT_MESSAGE);
    }

    @Expose
    @SerializedName(value = "projectID", alternate = {"projectid", "projectId"})
    private String projectID = "";

    public String getProjectID() {
        return Objects.requireNonNullElse(projectID, "");
    }

    public void setProjectID(String projectID) {
        if (projectID != null) {
            this.projectID = projectID;
        }
    }

}
