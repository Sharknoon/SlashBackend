package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;

import java.util.Objects;

public class GetChatMessage extends StatusAndSessionIDMessage {

    public GetChatMessage() {
        super(Status.GET_CHAT);
    }

    @Expose
    @SerializedName(value = "partnerUserID", alternate = {"partnerUserid", "partnerUserId"})
    private String partnerUserID = "";

    public String getPartnerUserID() {
        return Objects.requireNonNullElse(partnerUserID, "");
    }

    public void setPartnerUserID(String partnerUserID) {
        if (partnerUserID != null) {
            this.partnerUserID = partnerUserID;
        }
    }
}
