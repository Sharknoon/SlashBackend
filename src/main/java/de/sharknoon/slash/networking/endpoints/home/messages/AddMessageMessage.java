package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.database.models.MessageEmotion;
import de.sharknoon.slash.database.models.MessageType;

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
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        if (messageType != null) {
            this.messageType = messageType;
        }
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        if (messageContent != null) {
            this.messageContent = messageContent;
        }
    }

    public String getMessageSubject() {
        return messageSubject;
    }

    public void setMessageSubject(String messageSubject) {
        if (messageSubject != null) {
            this.messageSubject = messageSubject;
        }
    }

    public MessageEmotion getMessageEmotion() {
        return messageEmotion;
    }

    public void setMessageEmotion(MessageEmotion messageEmotion) {
        if (messageEmotion != null) {
            this.messageEmotion = messageEmotion;
        }
    }

    public String getMessageImage() {
        return messageImage;
    }

    public void setMessageImage(String messageImage) {
        if (messageImage != null) {
            this.messageImage = messageImage;
        }
    }
    
}
