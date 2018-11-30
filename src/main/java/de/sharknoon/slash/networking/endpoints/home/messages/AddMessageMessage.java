package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.database.models.message.MessageEmotion;
import de.sharknoon.slash.database.models.message.MessageType;

import java.util.Objects;

public abstract class AddMessageMessage extends StatusAndSessionIDMessage {

    @Expose
    private MessageType messageType = MessageType.NONE;
    @Expose
    private String messageContent = "";
    @Expose
    private String messageSubject = "";
    @Expose
    private MessageEmotion messageEmotion = MessageEmotion.NONE;
    @Expose
    private String messageImage = "";

    public MessageType getMessageType() {
        return Objects.requireNonNullElse(messageType, MessageType.NONE);
    }

    public void setMessageType(MessageType messageType) {
        if (messageType != null) {
            this.messageType = messageType;
        }
    }

    public String getMessageContent() {
        return Objects.requireNonNullElse(messageContent, "");
    }

    public void setMessageContent(String messageContent) {
        if (messageContent != null) {
            this.messageContent = messageContent;
        }
    }

    public String getMessageSubject() {
        return Objects.requireNonNullElse(messageSubject, "");
    }

    public void setMessageSubject(String messageSubject) {
        if (messageSubject != null) {
            this.messageSubject = messageSubject;
        }
    }

    public MessageEmotion getMessageEmotion() {
        return Objects.requireNonNullElse(messageEmotion, MessageEmotion.NONE);
    }

    public void setMessageEmotion(MessageEmotion messageEmotion) {
        if (messageEmotion != null) {
            this.messageEmotion = messageEmotion;
        }
    }

    public String getMessageImage() {
        return Objects.requireNonNullElse(messageImage, "");
    }

    public void setMessageImage(String messageImage) {
        if (messageImage != null) {
            this.messageImage = messageImage;
        }
    }

}
