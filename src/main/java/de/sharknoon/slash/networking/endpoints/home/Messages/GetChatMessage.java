package de.sharknoon.slash.networking.endpoints.home.Messages;

import com.google.gson.annotations.Expose;

public class GetChatMessage extends StatusAndSessionIDMessage {
    
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
