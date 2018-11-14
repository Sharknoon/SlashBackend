package de.sharknoon.slash.networking.endpoints.register;

import com.google.gson.annotations.Expose;

class RegisterMessage {
    @Expose
    private String username = "";
    @Expose
    private String email = "";
    @Expose
    private String password = "";
    
    String getUsername() {
        return username;
    }
    
    void setUsername(String username) {
        if (username != null) {
            this.username = username;
        }
    }
    
    String getEmail() {
        return email;
    }
    
    void setEmail(String email) {
        if (email != null) {
            this.email = email;
        }
    }
    
    String getPassword() {
        return password;
    }
    
    void setPassword(String password) {
        if (password != null) {
            this.password = password;
        }
    }
}