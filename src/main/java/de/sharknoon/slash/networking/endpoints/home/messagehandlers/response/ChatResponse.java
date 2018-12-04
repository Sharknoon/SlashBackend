package de.sharknoon.slash.networking.endpoints.home.messagehandlers.response;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.database.models.Chat;

public class ChatResponse {
    @Expose
    private final String status = "OK_CHAT";
    @Expose
    public Chat chat;
}
