package de.sharknoon.slash.networking.endpoints.home;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.database.models.message.Message;
import de.sharknoon.slash.database.models.message.MessageEmotion;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.*;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.ErrorResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.AddMessageMessage;
import de.sharknoon.slash.networking.endpoints.home.messages.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.sessions.LoginSessions;
import de.sharknoon.slash.properties.Properties;
import de.sharknoon.slash.utils.MimeTypeHelper;
import org.bson.types.ObjectId;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ServerEndpoint("/home")
public class HomeEndpoint extends Endpoint<StatusAndSessionIDMessage> {

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
        super(StatusAndSessionIDMessage.class);
        firstHandler.appendSuccessorToLast(new GetUserMessageHandler(this));
        firstHandler.appendSuccessorToLast(new LogoutMessageHandler(this));
        firstHandler.appendSuccessorToLast(new GetChatMessageHandler(this));
        firstHandler.appendSuccessorToLast(new AddProjectMessageHandler(this));
        firstHandler.appendSuccessorToLast(new GetProjectMessageHandler(this));
        firstHandler.appendSuccessorToLast(new GetUsersMessageHandler(this));
        firstHandler.appendSuccessorToLast(new ModifyProjectUsersMessageHandler(this));
        firstHandler.appendSuccessorToLast(new AddProjectMessageMessageHandler(this));
        firstHandler.appendSuccessorToLast(new AddChatMessageMessageHandler(this));
        firstHandler.appendSuccessorToLast(new NoneStatusMessageHandler(this));
        firstHandler.appendSuccessorToLast(new NullStatusMessageHandler(this));
    }
    
    @Override
    protected void onMessage(Session session, StatusAndSessionIDMessage message) {
        Optional<User> user = LoginSessions.getUser(message.getSessionid());
        
        if (user.isEmpty()) {//User not already logged in since the last server restart
            user = DB.getUserBySessionID(message.getSessionid());
            
            if (user.isEmpty()) {//User was never logged in
                send("{\"status\":\"NO_LOGIN_OR_TOO_MUCH_DEVICES\"," +
                        "\"messageType\":\"You are either not logged in or using more than " +
                        Properties.getUserConfig().maxdevices() + " devices\"}");
                return;
            }
        }
        LoginSessions.addSession(user.get(), message.getSessionid(), null, HomeEndpoint.class, session);
        handleLogic(message, user.get());
    }
    
    private void handleLogic(StatusAndSessionIDMessage message, User user) {
        firstHandler.handleMessage(message, user);
    }

    public Set<ObjectId> getAllExistingUserIDs(final List<String> userIDs) {
        return userIDs.parallelStream()
                .filter(ObjectId::isValid)
                .map(DB::getUserByUsernameOrEmail)
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
                //TODO: Save image on server, generate url, send url
                Base64.Decoder decoder = Base64.getDecoder();
                final String[] messageImageSplit = messageFromClient.getMessageImage().split("base64,");
                final String imageMime = messageImageSplit[0];
                final String imageContent = messageImageSplit[1];
                final byte[] imageData = decoder.decode(imageContent);
                try {
                    if (!MimeTypeHelper.hasValidMimeType(imageMime)) {
                        // ToDo: Return error
                        ErrorResponse error = new ErrorResponse();
                        error.status = "NOT_A_VALID_IMAGE";
                        error.description = "The image has not a valid mime type: " + imageMime + ". Should be one of: " + MimeTypeHelper.validMimeTypes.keySet().toString();
                        send(error);
                        return Optional.empty();
    
                    }
                    final String imageName = UUID.randomUUID().toString();
                    final String extension = MimeTypeHelper.getExtension(imageMime);
                    final String imageFullName = imageName + "." + extension;
                    final Path imageDirectory = Paths.get("").toAbsolutePath().getParent().resolve("webapps").resolve("slash").resolve("img");
                    Files.createDirectories(imageDirectory);
                    final Path imagePath = imageDirectory.resolve(imageFullName);
                    Files.write(imagePath, imageData);
    
                    String baseURL = "";
                    newMessage.imageUrl = new URL(baseURL + "img/" + imageFullName);
                } catch (MalformedURLException e) {
                    Logger.getGlobal().severe("Image has no correct URL " + e);
                } catch (IOException e) {
                    Logger.getGlobal().severe("Could not read or write Image! " + e);
                    e.printStackTrace();
                }
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
