package de.sharknoon.slash.networking.endpoints.login;

public class LoginMessage {
    private String usernameOrEmail = "";
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