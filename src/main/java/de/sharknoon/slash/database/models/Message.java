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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (!subject.equals(message.subject)) return false;
        if (!content.equals(message.content)) return false;
        if (!creationDate.equals(message.creationDate)) return false;
        return sender != null ? sender.equals(message.sender) : message.sender == null;
    }

    @Override
    public int hashCode() {
        int result = subject.hashCode();
        result = 31 * result + content.hashCode();
        result = 31 * result + creationDate.hashCode();
        result = 31 * result + (sender != null ? sender.hashCode() : 0);
        return result;
    }
}
