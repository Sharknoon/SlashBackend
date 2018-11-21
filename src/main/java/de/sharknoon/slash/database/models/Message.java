package de.sharknoon.slash.database.models;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import java.net.URL;
import java.time.LocalDateTime;

public class Message {
    @Expose
    public MessageType type = MessageType.TEXT;
    @Expose
    public String subject = StringUtils.EMPTY;
    @Expose
    public String content = StringUtils.EMPTY;
    @Expose
    public LocalDateTime creationDate = LocalDateTime.now();
    @Expose
    public ObjectId sender = new ObjectId();
    @Expose
    public EmotionCategory emotionCategory = EmotionCategory.NONE;

    @Expose
    public URL imageUrl;


    public Message(final String subject, final String content, final LocalDateTime timestamp, final EmotionCategory color) {
        this.subject = subject;
        this.content = content;
        this.creationDate = timestamp;
        this.emotionCategory = color;
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

    public EmotionCategory getEmotionCategory() {
        return emotionCategory;
    }

    public URL getImageUrl() {
        return imageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (type != message.type) return false;
        if (!subject.equals(message.subject)) return false;
        if (!content.equals(message.content)) return false;
        if (!creationDate.equals(message.creationDate)) return false;
        if (!sender.equals(message.sender)) return false;
        if (emotionCategory != message.emotionCategory) return false;
        return imageUrl.equals(message.imageUrl);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + subject.hashCode();
        result = 31 * result + content.hashCode();
        result = 31 * result + creationDate.hashCode();
        result = 31 * result + sender.hashCode();
        result = 31 * result + emotionCategory.hashCode();
        result = 31 * result + imageUrl.hashCode();
        return result;
    }
}
