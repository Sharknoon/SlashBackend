package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.networking.endpoints.home.Status;

public class GetChatMessage extends StatusAndSessionIDMessage {

    public GetChatMessage() {
        setStatus(Status.GET_CHAT);
    }

    @Expose
    private String partnerUserID = "";

    public String getPartnerUserID() {
        return partnerUserID;
    }

    public void setPartnerUserID(String partnerUserID) {
        if (partnerUserID != null) {
            this.partnerUserID = partnerUserID;
        }
    }
}
