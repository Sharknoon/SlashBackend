package de.sharknoon.slash.database.models;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Set;

public class User {
    
    public ObjectId id;
    public String username;
    public String email;
    public String password;
    public String salt;
    public LocalDateTime registrationDate;
    public Set<String> sessionIDs;
    
}
