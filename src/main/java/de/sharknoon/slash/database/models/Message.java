package de.sharknoon.slash.database.models;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import java.net.URL;
import java.time.LocalDateTime;

public class Message {
    @Expose
    public ObjectId sender = new ObjectId();
    @Expose
    public LocalDateTime creationDate = LocalDateTime.now().withNano(0);
    @Expose
    public MessageType type = MessageType.NONE;
    @Expose
    public String content = StringUtils.EMPTY;
    //No default value because GSON doesnt serialize null values
    @Expose
    public String subject;
    //No default value because GSON doesnt serialize null values
    @Expose
    public MessageEmotion emotion;
    //No default value because GSON doesnt serialize null values
    @Expose
    public URL imageUrl;
    
}
