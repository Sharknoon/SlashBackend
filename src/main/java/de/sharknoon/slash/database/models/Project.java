package de.sharknoon.slash.database.models;

import com.google.gson.annotations.Expose;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Set;
public class Project {
    
    @BsonId
    @Expose
    public ObjectId id;
    @Expose
    public String name;
    @Expose
    public String image;
    @Expose
    public LocalDateTime creationDate;
    @Expose
    public Set<ObjectId> users;
    
}
