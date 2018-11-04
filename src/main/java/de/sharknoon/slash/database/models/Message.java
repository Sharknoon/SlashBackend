package de.sharknoon.slash.database.models;

import com.google.gson.annotations.Expose;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public class Message {
    
    @Expose
    ObjectId id;
    @Expose
    LocalDateTime creationDate;
    @Expose
    MessageTypes type;
    @Expose
    Object message;
    
    public enum MessageTypes {
        TEXT
    }
    
}
