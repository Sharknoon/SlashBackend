package de.sharknoon.slash.database.models;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import java.net.*;
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
    @Expose
    public String subject = StringUtils.EMPTY;
    @Expose
    public MessageEmotion messageEmotion = MessageEmotion.NONE;
    @Expose
    public URL imageUrl;
    
    {
        try {
            imageUrl = new URL("");
        } catch (MalformedURLException ignored) {
        }
    }
    
}
