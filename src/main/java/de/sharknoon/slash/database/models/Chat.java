package de.sharknoon.slash.database.models;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.database.models.message.Message;
import org.apache.commons.lang3.StringUtils;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

public class Chat {
    
    @BsonId
    @Expose
    public ObjectId id = new ObjectId();
    @Expose
    public ObjectId personA = new ObjectId();
    @Expose
    public ObjectId personB = new ObjectId();
    @BsonIgnore
    @Expose
    public String partnerUsername = StringUtils.EMPTY;
    @Expose
    public LocalDateTime creationDate = LocalDateTime.now();
    @Expose
    public Set<Message> messages = Collections.emptySet();
}
