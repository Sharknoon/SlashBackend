package de.sharknoon.slash.networking.endpoints.home.handlers.response;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.database.models.User;

public class UserResponse {
    @Expose
    private static final String status = "OK_USER";
    @Expose
    public User user;
}
