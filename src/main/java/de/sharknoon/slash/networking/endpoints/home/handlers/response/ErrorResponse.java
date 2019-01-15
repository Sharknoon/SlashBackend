package de.sharknoon.slash.networking.endpoints.home.handlers.response;

import com.google.gson.annotations.Expose;

public class ErrorResponse {
    @Expose
    public String status;
    @Expose
    public String description;
}
