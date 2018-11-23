package de.sharknoon.slash.networking.endpoints.home;

import com.google.gson.annotations.Expose;

class GetChatMessage extends StatusAndSessionIDMessage {
    
    @Expose
    private String partnerUserID = "";
    
    String getPartnerUserID() {
        return partnerUserID;
    }
    
    void setPartnerUserID(String partnerUserID) {
        if (partnerUserID != null) {
            this.partnerUserID = partnerUserID;
        }
    }
}
