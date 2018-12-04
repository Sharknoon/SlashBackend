package de.sharknoon.slash.networking.endpoints.home.messagehandlers.response;

import com.google.gson.annotations.Expose;

public class ErrorResponse {
    @Expose
    public String status;
    @Expose
    public String description;
}
