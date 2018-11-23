package de.sharknoon.slash.networking.endpoints.home;

import com.google.gson.annotations.Expose;

class AddChatMessageMessage extends AddMessageMessage {
    
    @Expose
    private String chatID = "";
    
    String getChatID() {
        return chatID;
    }
    
    void setChatID(String chatID) {
        if (chatID != null) {
            this.chatID = chatID;
        }
    }
    
}
