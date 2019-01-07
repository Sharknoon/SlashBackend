package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import de.sharknoon.slash.networking.endpoints.Status;

import java.util.Objects;

public class AddChatMessageMessage extends AddMessageMessage {

    public AddChatMessageMessage() {
        super(Status.ADD_CHAT_MESSAGE);
    }

    @Expose
    @SerializedName(value = "chatID", alternate = {"chatid", "chatId"})
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
