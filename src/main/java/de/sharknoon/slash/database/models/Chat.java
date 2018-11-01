package de.sharknoon.slash.database.models;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public class Chat {
    
    public ObjectId id;
    public String personA;
    public String personB;
    public LocalDateTime creationDate;
    
    
}
