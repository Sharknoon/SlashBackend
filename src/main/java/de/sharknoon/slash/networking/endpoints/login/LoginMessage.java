package de.sharknoon.slash.networking.endpoints.login;

import com.google.gson.annotations.Expose;

import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class LoginMessage {
    @Expose
    private String usernameOrEmail = "";
    @Expose
    private String password = "";
    @Expose
    private String deviceID = "";

    public String getUsernameOrEmail() {
        return Objects.requireNonNullElse(usernameOrEmail, "");
    }

    public void setUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail != null) {
            this.usernameOrEmail = usernameOrEmail;
        }
    }

    public String getPassword() {
        return Objects.requireNonNullElse(password, "");
    }

    public void setPassword(String password) {
        if (password != null) {
            this.password = password;
        }
    }

    public String getDeviceID() {
        return Objects.requireNonNullElse(deviceID, "");
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
}