package de.sharknoon.slash.database.models;

import com.google.gson.annotations.Expose;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Set;

public class Chat {
    
    @BsonId
    @Expose
    public ObjectId id;
    @Expose
    public ObjectId personA;
    @Expose
    public ObjectId personB;
    @BsonIgnore
    @Expose
    public String partnerUsername;
    @Expose
    public LocalDateTime creationDate;
    @Expose
    public Set<Message> messages;
}
