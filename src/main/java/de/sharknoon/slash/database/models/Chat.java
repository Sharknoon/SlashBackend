package de.sharknoon.slash.database.models;

import com.google.gson.annotations.Expose;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;

public class Chat {
    
    @BsonId
    @Expose
    public ObjectId id;
    //Person A is always you!
    @Expose
    public ObjectId personA;
    @Expose
    public ObjectId personB;
    //TMP
    @Expose
    public String personBUsername;
    @Expose
    public LocalDateTime creationDate;
    //The IDs of the messages
    @Expose
    public List<String> messages;
    
}
