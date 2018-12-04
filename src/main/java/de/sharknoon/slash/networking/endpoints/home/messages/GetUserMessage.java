package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import de.sharknoon.slash.networking.endpoints.home.Status;

import java.util.Objects;

public class GetUserMessage extends StatusAndSessionIDMessage {

    @Expose
    @SerializedName(value = "userID", alternate = {"userId", "userid"})
    private String userID = "";

    public GetUserMessage() {
        setStatus(Status.GET_USER);
    }

    public String getUserID() {
        return Objects.requireNonNullElse(userID, "");
    }

    public void setUserID(String userID) {
        if (userID != null) {
            this.userID = userID;
        }
    }
}
