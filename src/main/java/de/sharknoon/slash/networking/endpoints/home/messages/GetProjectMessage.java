package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.networking.endpoints.home.Status;

import java.util.Objects;

public class GetProjectMessage extends StatusAndSessionIDMessage {

    public GetProjectMessage() {
        setStatus(Status.GET_PROJECT);
    }

    @Expose
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
