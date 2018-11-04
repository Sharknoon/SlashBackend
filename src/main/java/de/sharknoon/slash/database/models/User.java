package de.sharknoon.slash.database.models;

import com.google.gson.annotations.Expose;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Set;

public class User {
    
    @Expose
    public ObjectId id;
    @Expose
    public String username;
    public String email;
    public String password;
    public String salt;
    public LocalDateTime registrationDate;
    public Set<String> sessionIDs;
    
}
