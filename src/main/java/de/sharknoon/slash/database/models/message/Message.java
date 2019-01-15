package de.sharknoon.slash.database.models.message;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Objects;

public class Message {
    @Expose
    public MessageType type = MessageType.NONE;
    //No default value because GSON doesnt serialize null values
    @Expose
    public String subject;
    @Expose
    public String content = StringUtils.EMPTY;
    @Expose
    public LocalDateTime creationDate = LocalDateTime.now();
    @Expose
    public ObjectId sender = new ObjectId();
    //No default value because GSON doesnt serialize null values
    @Expose
    public MessageEmotion emotion;
    //No default value because GSON doesnt serialize null values
    @Expose
    public ObjectId image;


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

    public ObjectId getImage() {
        return image;
    }

    public MessageEmotion getEmotion() {
        return emotion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (type != message.type) return false;
        if (!Objects.equals(subject, message.subject)) return false;
        if (!Objects.equals(content, message.content)) return false;
        if (!Objects.equals(creationDate, message.creationDate)) return false;
        if (!Objects.equals(sender, message.sender)) return false;
        if (emotion != message.emotion) return false;
        return Objects.equals(image, message.image);
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (subject != null ? subject.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (sender != null ? sender.hashCode() : 0);
        result = 31 * result + (emotion != null ? emotion.hashCode() : 0);
        result = 31 * result + (image != null ? image.hashCode() : 0);
        return result;
    }
}
