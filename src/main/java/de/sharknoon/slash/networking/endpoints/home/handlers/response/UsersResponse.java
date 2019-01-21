package de.sharknoon.slash.networking.endpoints.home.handlers.response;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.database.models.User;

import java.util.Set;

public class UsersResponse {
    @Expose
    private static final String status = "OK_USERS";
    @Expose
    public Set<User> users;
}
