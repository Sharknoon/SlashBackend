package de.sharknoon.slash.networking.endpoints.home.handlers.response;

import com.google.gson.annotations.Expose;

public class ImageResponse {
    @Expose
    private static final String status = "OK_IMAGE";
    @Expose
    public String imageID;
}
