package de.sharknoon.slash.networking.endpoints.home;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.database.models.message.Message;
import de.sharknoon.slash.database.models.message.MessageEmotion;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.endpoints.file.FileEndpoint;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.*;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.ErrorResponse;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.ImageResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.AddMessageMessage;
import de.sharknoon.slash.networking.sessions.LoginSessionUtils;
import de.sharknoon.slash.serialisation.Serialisation;
import org.bson.types.ObjectId;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ServerEndpoint(
        value = "/home"
)
public class HomeEndpoint extends Endpoint {

    private HomeEndpointMessageHandler firstHandler = new GetHomeMessageHandler(this);

    private static boolean isNotValidChatMessageContent(String content) {
        return content.length() <= 0 || content.length() >= 5000;
    }

    private static boolean isNotValidMessageSubject(String subject) {
        return subject.length() <= 0 || subject.length() >= 100;
    }

    private static boolean isNotValidMessageEmotion(MessageEmotion emotion) {
        return emotion == MessageEmotion.NONE || emotion == null;
    }

    //Needs to stay public because of the endpoints
    public HomeEndpoint() {
        firstHandler.appendSuccessorToLast(new GetUserMessageHandler(this));
        firstHandler.appendSuccessorToLast(new LogoutMessageHandler(this));
        firstHandler.appendSuccessorToLast(new GetChatMessageHandler(this));
        firstHandler.appendSuccessorToLast(new AddProjectMessageHandler(this));
        firstHandler.appendSuccessorToLast(new GetProjectMessageHandler(this));
        firstHandler.appendSuccessorToLast(new GetUsersMessageHandler(this));
        firstHandler.appendSuccessorToLast(new ModifyProjectUsersMessageHandler(this));
        firstHandler.appendSuccessorToLast(new ModifyProjectOwnerMessageHandler(this));
        firstHandler.appendSuccessorToLast(new AddProjectMessageMessageHandler(this));
        firstHandler.appendSuccessorToLast(new AddChatMessageMessageHandler(this));
        firstHandler.appendSuccessorToLast(new NoneStatusMessageHandler(this));
        firstHandler.appendSuccessorToLast(new NullStatusMessageHandler(this));
    }

    @OnMessage
    public void onTextMessage(Session session, String message) {
        handleMessage(message, () -> {
            StatusAndSessionIDMessage statusAndSessionIDMessage = Serialisation.getGSON().fromJson(message, StatusAndSessionIDMessage.class);
            Optional<User> optionalUser = LoginSessionUtils.checkLogin(statusAndSessionIDMessage.getSessionid(), session, this);
            if (optionalUser.isEmpty()) {
                return;
            }

            firstHandler.handleMessage(statusAndSessionIDMessage, optionalUser.get());
        });
    }

    public Set<ObjectId> getAllExistingUserIDs(final List<String> userIDs) {
        return userIDs.parallelStream()
                .filter(ObjectId::isValid)
                .map(ObjectId::new)
                .map(DB::getUser)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(u -> u.id)
                .collect(Collectors.toSet());
    }

    public Optional<Message> fillMessage(AddMessageMessage messageFromClient, User sender, boolean isChat) {
        String communicationType = isChat ? "CHAT" : "PROJECT";
        Message newMessage = new Message();
        newMessage.type = messageFromClient.getMessageType();
        newMessage.creationDate = LocalDateTime.now().withNano(0);
        switch (messageFromClient.getMessageType()) {
            case EMOTION:
                if (isNotValidMessageSubject(messageFromClient.getMessageSubject())) {
                    //The chat subject isn't valid
                    ErrorResponse error = new ErrorResponse();
                    error.status = communicationType + "_MESSAGE_SUBJECT_TOO_LONG";
                    error.description = "The " + communicationType.toLowerCase() + " message subject was too long";
                    send(error);
                    return Optional.empty();
                }
                if (isNotValidMessageEmotion(messageFromClient.getMessageEmotion())) {
                    //The chat subject isn't valid
                    ErrorResponse error = new ErrorResponse();
                    error.status = communicationType + "_MESSAGE_EMOTION_NOT_SET";
                    error.description = "The " + communicationType.toLowerCase() + " message emotion was not set";
                    send(error);
                    return Optional.empty();
                }
                newMessage.subject = messageFromClient.getMessageSubject();
                newMessage.emotion = messageFromClient.getMessageEmotion();
                //no break or return!
            case TEXT:
                if (isNotValidChatMessageContent(messageFromClient.getMessageContent())) {
                    //Chat messageType malformed
                    ErrorResponse error = new ErrorResponse();
                    error.status = communicationType + "_MESSAGE_CONTENT_TOO_LONG";
                    error.description = "The " + communicationType.toLowerCase() + " message content was over 5000 characters long";
                    send(error);
                    return Optional.empty();
                }
                newMessage.content = messageFromClient.getMessageContent();
                newMessage.creationDate = LocalDateTime.now().withNano(0);
                newMessage.sender = sender.id;
                return Optional.of(newMessage);
            case IMAGE:
                //Constructing new ID for the future image to be uploaded
                ObjectId newImageObjectID = new ObjectId();
                String newImageID = newImageObjectID.toHexString();
                ImageResponse ir = new ImageResponse();
                ir.imageID = newImageID;
                //Sending the imageID to the user to allow for the upload
                send(ir);
                //Adding the id for allowing upload access
                FileEndpoint.allowUpload(newImageID);
                newMessage.image = newImageObjectID;
                return Optional.of(newMessage);
            case NONE:
                //The chat message type is invalid
                ErrorResponse error = new ErrorResponse();
                error.status = communicationType + "_MESSAGE_TYPE_INVALID";
                error.description = "The " + communicationType.toLowerCase() + " message type doesn't match the specification";
                send(error);
                return Optional.empty();
        }
        return Optional.empty();
    }
}
