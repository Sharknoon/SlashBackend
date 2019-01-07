package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;

import java.util.Objects;

public class GetProjectMessage extends StatusAndSessionIDMessage {

    public GetProjectMessage() {
        super(Status.GET_PROJECT);
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
