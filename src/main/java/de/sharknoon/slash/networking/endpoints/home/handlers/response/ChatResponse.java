package de.sharknoon.slash.networking.endpoints.home.handlers.response;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.database.models.Chat;

public class ChatResponse {
    @Expose
    private static final String status = "OK_CHAT";
    @Expose
    public Chat chat;
}
