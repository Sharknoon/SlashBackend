package de.sharknoon.slash.networking.endpoints.home;

import com.google.gson.annotations.Expose;

@SuppressWarnings("ALL")
public class HomeMessage {
    
    //General
    @Expose
    private String sessionid = "";
    @Expose
    private String status = "";
    //Add Project
    @Expose
    private String projectName = "";
    //Get Project
    @Expose
    private String projectID = "";
    //Get Chat
    @Expose
    private String partnerUserID = "";
    //Search user by username
    @Expose
    private String username = "";
    
    public String getSessionid() {
        return sessionid;
    }
    
    public void setSessionid(String sessionid) {
        if (sessionid != null) {
            this.sessionid = sessionid;
        }
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        if (status != null) {
            this.status = status;
        }
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        if (projectName != null) {
            this.projectName = projectName;
        }
    }
    
    public String getProjectID() {
        return projectID;
    }
    
    public void setProjectID(String projectID) {
        if (projectID != null) {
            this.projectID = projectID;
        }
    }
    
    public String getPartnerUserID() {
        return partnerUserID;
    }
    
    public void setPartnerUserID(String partnerUserID) {
        if (partnerUserID != null) {
            this.partnerUserID = partnerUserID;
        }
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        if (username != null) {
            this.username = username;
        }
    }
}
