package de.sharknoon.slash.networking.endpoints.home.handlers.response;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.database.models.Project;

public class ProjectResponse {
    @Expose
    private final String status = "OK_PROJECT";
    @Expose
    public Project project;
}
