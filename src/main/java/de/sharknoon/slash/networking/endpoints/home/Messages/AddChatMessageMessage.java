package de.sharknoon.slash.networking.endpoints.home.Messages;

import com.google.gson.annotations.Expose;

public class AddChatMessageMessage extends AddMessageMessage {

    @Expose
    private String chatID = "";

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        if (chatID != null) {
            this.chatID = chatID;
        }
    }

}
