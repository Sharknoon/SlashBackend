package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.networking.endpoints.home.Status;

public class ModifyProjectUsersMessage extends StatusAndSessionIDMessage {
    
    @Expose
    private String projectID = "";
    
    @Expose
    private String userID = "";
    
    @Expose
    private boolean addUser = true;
    
    public ModifyProjectUsersMessage() {
        setStatus(Status.MODIFY_PROJECT_USERS);
    }
    
    public String getProjectID() {
        return projectID;
    }
    
    public void setProjectID(String projectID) {
        if (projectID != null) {
            this.projectID = projectID;
        }
    }
    
    public String getUserID() {
        return userID;
    }
    
    public void setUserID(String userID) {
        if (userID != null) {
            this.userID = userID;
        }
    }
    
    public boolean isAddUser() {
        return addUser;
    }
    
    public void setAddUser(boolean addUser) {
        this.addUser = addUser;
    }
}
