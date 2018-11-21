package de.sharknoon.slash.database.models;

import com.google.gson.annotations.Expose;
import org.bson.types.ObjectId;

import java.net.URL;
import java.time.LocalDateTime;

public class Message {
    @Expose
    public MessageType type;
    @Expose
    public String subject;
    @Expose
    public String content;
    @Expose
    public LocalDateTime creationDate;
    @Expose
    public ObjectId sender;
    @Expose
    public EmotionCategory emotionCategory;
    @Expose
    public URL imageUrl;
    
}
