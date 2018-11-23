package de.sharknoon.slash.networking.endpoints.home;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.database.models.*;

class AddMessageMessage extends StatusAndSessionIDMessage {
    
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
    
    MessageType getMessageType() {
        return messageType;
    }
    
    void setMessageType(MessageType messageType) {
        if (messageType != null) {
            this.messageType = messageType;
        }
    }
    
    String getMessageContent() {
        return messageContent;
    }
    
    void setMessageContent(String messageContent) {
        if (messageContent != null) {
            this.messageContent = messageContent;
        }
    }
    
    String getMessageSubject() {
        return messageSubject;
    }
    
    void setMessageSubject(String messageSubject) {
        if (messageSubject != null) {
            this.messageSubject = messageSubject;
        }
    }
    
    MessageEmotion getMessageEmotion() {
        return messageEmotion;
    }
    
    void setMessageEmotion(MessageEmotion messageEmotion) {
        if (messageEmotion != null) {
            this.messageEmotion = messageEmotion;
        }
    }
    
    String getMessageImage() {
        return messageImage;
    }
    
    void setMessageImage(String messageImage) {
        if (messageImage != null) {
            this.messageImage = messageImage;
        }
    }
    
}
