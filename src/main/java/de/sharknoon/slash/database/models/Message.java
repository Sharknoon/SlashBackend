package de.sharknoon.slash.database.models;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import java.net.URL;
import java.time.LocalDateTime;

public class Message {
    @Expose
    public MessageType type = MessageType.NONE;
    @Expose
    public String subject;  //No default value because GSON doesnt serialize null values
    @Expose
    public String content = StringUtils.EMPTY;
    @Expose
    public LocalDateTime creationDate = LocalDateTime.now();
    @Expose
    public ObjectId sender = new ObjectId();
    @Expose
    public MessageEmotion emotion;
    //No default value because GSON doesnt serialize null values
    @Expose
    public URL imageUrl;


    public Message() {
    }

    public Message(final String subject, final String content, final LocalDateTime timestamp, final MessageEmotion emotion) {
        this.subject = subject;
        this.content = content;
        this.creationDate = timestamp;
        this.emotion = emotion;
    }


    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public ObjectId getSender() {
        return sender;
    }

    public MessageType getType() {
        return type;
    }

    public URL getImageUrl() {
        return imageUrl;
    }

    public MessageEmotion getEmotion() {
        return emotion;
    }

}
