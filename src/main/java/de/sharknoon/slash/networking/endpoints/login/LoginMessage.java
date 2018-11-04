package de.sharknoon.slash.networking.endpoints.login;

import com.google.gson.annotations.Expose;

public class LoginMessage {
    @Expose
    private String usernameOrEmail = "";
    @Expose
    private String password = "";
    
    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }
    
    public void setUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail != null) {
            this.usernameOrEmail = usernameOrEmail;
        }
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        if (password != null) {
            this.password = password;
        }
    }
}