package de.sharknoon.slash.database.models;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.database.models.message.Message;
import de.sharknoon.slash.networking.aylien.Sentiment;
import org.apache.commons.lang3.StringUtils;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

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
    public ObjectId image;
    @Expose
    public LocalDateTime creationDate = LocalDateTime.now();
    public Set<ObjectId> users = Set.of();
    @BsonIgnore
    @Expose
    public Set<User> usernames = Set.of();
    @Expose
    public ObjectId projectOwner = new ObjectId();
    @BsonIgnore
    @Expose
    public Sentiment sentiment = new Sentiment();
    @Expose
    public Set<Message> messages = Set.of();
}
