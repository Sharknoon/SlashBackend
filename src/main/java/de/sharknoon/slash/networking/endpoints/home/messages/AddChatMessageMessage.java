package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.networking.endpoints.home.Status;

import java.util.Objects;

public class AddChatMessageMessage extends AddMessageMessage {

    public AddChatMessageMessage() {
        setStatus(Status.ADD_CHAT_MESSAGE);
    }

    @Expose
    private String chatID = "";

    public String getChatID() {
        return Objects.requireNonNullElse(chatID, "");
    }

    public void setChatID(String chatID) {
        if (chatID != null) {
            this.chatID = chatID;
        }
    }

}
