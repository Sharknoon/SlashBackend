package de.sharknoon.slash.database.models;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Set;

public class Project {
    
    public ObjectId id;
    public String name;
    public String image;
    public LocalDateTime creationDate;
    public Set<String> users;
    
    
}
