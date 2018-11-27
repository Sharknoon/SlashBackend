package de.sharknoon.slash.database.models;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.database.models.message.Message;
import org.apache.commons.lang3.StringUtils;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Set;
public class Project {
    
    @BsonId
    @Expose
    public ObjectId id = new ObjectId();
    @Expose
    public String name = StringUtils.EMPTY;
    @Expose
    public String description = StringUtils.EMPTY;
    @Expose
    public URL image;
    @Expose
    public LocalDateTime creationDate = LocalDateTime.now();
    @Expose
    public Set<ObjectId> users = Set.of();
    @Expose
    public Set<Message> messages = Set.of();
}
