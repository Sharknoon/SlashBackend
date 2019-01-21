package de.sharknoon.slash.networking.endpoints.home.handlers.response;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.database.models.Chat;
import de.sharknoon.slash.database.models.Project;

import java.util.Set;

public class HomeResponse {
    @Expose
    private static final String status = "OK_HOME";
    @Expose
    public Set<Project> projects;
    @Expose
    public Set<Chat> chats;
}