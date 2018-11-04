package de.sharknoon.slash.networking.endpoints.register;

import com.google.gson.annotations.Expose;

public class RegisterMessage {
    @Expose
    private String username = "";
    @Expose
    private String email = "";
    @Expose
    private String password = "";
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        if (username != null) {
            this.username = username;
        }
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        if (email != null) {
            this.email = email;
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