package de.sharknoon.slash.networking.endpoints.home.messagehandlers.response;

import com.google.gson.annotations.Expose;

public class ImageResponse {
    @Expose
    private final String status = "OK_IMAGE";
    @Expose
    public String imageID;
}
