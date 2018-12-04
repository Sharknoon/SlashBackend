package de.sharknoon.slash.networking.endpoints.home.messagehandlers.response;

import com.google.gson.annotations.Expose;

public class LogoutResponse {
    @Expose
    private final String status = "OK_LOGOUT";
}
