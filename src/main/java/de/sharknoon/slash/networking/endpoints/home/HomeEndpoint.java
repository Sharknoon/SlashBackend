package de.sharknoon.slash.networking.endpoints.home;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.*;
import de.sharknoon.slash.database.models.message.*;
import de.sharknoon.slash.networking.LoginSessions;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import de.sharknoon.slash.networking.endpoints.home.messages.*;
import de.sharknoon.slash.networking.pushy.*;
import de.sharknoon.slash.networking.utils.MimeTypeHelper;
import de.sharknoon.slash.properties.Properties;
import de.sharknoon.slash.serialisation.Serialisation;
import org.bson.types.ObjectId;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ServerEndpoint("/home")
public class HomeEndpoint extends Endpoint<StatusAndSessionIDMessage> {
    
    
    private static boolean isNotValidChatMessageContent(String content) {
        return content.length() <= 0 || content.length() >= 5000;
    }
    
    private static boolean isNotValidMessageSubject(String subject) {
        return subject.length() <= 0 || subject.length() >= 100;
    }
    
    private static boolean isNotValidMessageEmotion(MessageEmotion emotion) {
        return emotion == MessageEmotion.NONE;
    }
    
    //Needs to stay public because of the endpoints
    @SuppressWarnings("WeakerAccess")
    public HomeEndpoint() {
        super(StatusAndSessionIDMessage.class);
    }
    
    private boolean isValidProjectName(String projectName) {
        return projectName.length() > 0 && projectName.length() < 20;
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
        switch (message.getStatus()) {
            case GET_HOME:
                handleGetHomeLogic(user);
                break;
            case GET_CHAT:
                handleGetChatLogic(user);
                break;
            case ADD_PROJECT:
                handleAddProjectLogic(user);
                break;
            case GET_PROJECT:
                handleGetProjectLogic();
                break;
            case GET_USERS:
                handleGetUsersLogic();
                break;
            case ADD_CHAT_MESSAGE:
                handleAddChatMessageLogic(user);
                break;
            case ADD_PROJECT_MESSAGE:
                handleAddProjectMessageLogic(user);
                break;
            case LOGOUT:
                handleLogoutLogic(user, message.getSessionid());
                break;
            case GET_USER:
                handleGetUserLogic();
                break;
            case MODIFY_PROJECT_USERS:
                handleModifyProjectUsersLogic(user);
                break;
            case NONE:
            default:
                ErrorResponse error = new ErrorResponse();
                error.status = "WRONG_STATUS";
                error.description = "The status was wrong, please check the API";
                send(error);
        }
    }
    
    private void handleAddProjectMessageLogic(User user) {
        AddProjectMessageMessage addProjectMessageMessage = Serialisation.getGSON().fromJson(getLastMessage(), AddProjectMessageMessage.class);
        Optional<Project> project;
        if (!ObjectId.isValid(addProjectMessageMessage.getProjectID())) {
            //wrong chat id syntax
            ErrorResponse error = new ErrorResponse();
            error.status = "WRONG_PROJECT_ID";
            error.description = "The syntax of the project-ID was not correct";
            send(error);
        } else if ((project = DB.getProject(new ObjectId(addProjectMessageMessage.getProjectID()))).isEmpty()) {
            //chat id doesn't exist
            ErrorResponse error = new ErrorResponse();
            error.status = "NO_PROJECT_FOUND";
            error.description = "No corresponding project to the project-ID was found";
            send(error);
        } else {
            Optional<Message> optionalMessage = fillMessage(addProjectMessageMessage, user, false);
            //Errors already send
            if (optionalMessage.isEmpty()) {
                return;
            }
            Message message = optionalMessage.get();
            Project p = project.get();
            DB.addMessageToProject(p, message);
            //Project specific, send to every user of the project
            Set<User> users = p.users.parallelStream()
                    .map(DB::getUser)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            ProjectResponse pr = new ProjectResponse();
            pr.project = p;
            LoginSessions.getSessions(HomeEndpoint.class, users).forEach(session -> sendTo(session, pr));
            //Dont want to send the push notification to myself
            users.remove(user);
            Pushy.sendPush(PushStatus.NEW_PROJECT_MESSAGE, message, user.username, users);
        }
    }
    
    private void handleAddChatMessageLogic(User user) {
        AddChatMessageMessage addChatMessageMessage = Serialisation.getGSON().fromJson(getLastMessage(), AddChatMessageMessage.class);
        Optional<Chat> chat;
        if (!ObjectId.isValid(addChatMessageMessage.getChatID())) {
            //wrong chat id syntax
            ErrorResponse error = new ErrorResponse();
            error.status = "WRONG_CHAT_ID";
            error.description = "The syntax of the chat-ID was not correct";
            send(error);
        } else if ((chat = DB.getChat(new ObjectId(addChatMessageMessage.getChatID()))).isEmpty()) {
            //chat id doesn't exist
            ErrorResponse error = new ErrorResponse();
            error.status = "NO_CHAT_FOUND";
            error.description = "No corresponding chat to the chat-ID was found";
            send(error);
        } else {
            Chat c = chat.get();
            Optional<User> partner;
            if (Objects.equals(c.personA, user.id)) {//I am user a
                partner = DB.getUser(c.personB);
            } else {
                partner = DB.getUser(c.personA);
            }
            if (partner.isEmpty()) {
                //partner doesn't exists
                ErrorResponse error = new ErrorResponse();
                error.status = "CHAT_PARTNER_NOT_FOUND";
                error.description = "No corresponding chat partner to the chat-ID was found";
                send(error);
            } else {
                Optional<Message> optionalMessage = fillMessage(addChatMessageMessage, user, true);
                //errors already send
                if (optionalMessage.isEmpty()) {
                    return;
                }
                Message message = optionalMessage.get();
                DB.addMessageToChat(c, message);
                ChatResponse cr = new ChatResponse();
                cr.chat = c;
                c.partnerUsername = partner.get().username;
                LoginSessions.getSession(HomeEndpoint.class, user).ifPresent(session -> send(cr));
                Pushy.sendPush(PushStatus.NEW_CHAT_MESSAGE, message, user.username, partner.get());
                c.partnerUsername = user.username;
                LoginSessions.getSession(HomeEndpoint.class, partner.get()).ifPresent(session -> send(cr));
            }
        }
    }
    
    private void handleGetUsersLogic() {
        GetUsersMessage getUsersMessage = Serialisation.getGSON().fromJson(getLastMessage(), GetUsersMessage.class);
        Set<User> foundUsers = DB.searchUsers(getUsersMessage.getSearch());
        UsersResponse um = new UsersResponse();
        um.users = foundUsers;
        send(um);
    }
    
    private void handleGetProjectLogic() {
        GetProjectMessage getProjectMessage = Serialisation.getGSON().fromJson(getLastMessage(), GetProjectMessage.class);
        if (!ObjectId.isValid(getProjectMessage.getProjectID())) {
            ErrorResponse error = new ErrorResponse();
            error.status = "WRONG_PROJECT_ID";
            error.description = "The specified projectID doesn't conform to the right syntax";
            send(error);
        } else {
            ObjectId projectID = new ObjectId(getProjectMessage.getProjectID());
            Optional<Project> project = DB.getProject(projectID);
            if (project.isPresent()) {
                ProjectResponse pm = new ProjectResponse();
                pm.project = project.get();
                send(pm);
            } else {
                ErrorResponse error = new ErrorResponse();
                error.status = "NO_PROJECT_FOUND";
                error.description = "No project with the specified id was found";
                send(error);
            }
        }
    }
    
    private void handleAddProjectLogic(User user) {
        AddProjectMessage addProjectMessage = Serialisation.getGSON().fromJson(getLastMessage(), AddProjectMessage.class);
        String projectName = addProjectMessage.getProjectName();
        String projectDescription = addProjectMessage.getProjectDescription();
        List<String> memberIDs = addProjectMessage.getMemberIDs();
        if (!isValidProjectName(projectName)) {
            ErrorResponse error = new ErrorResponse();
            error.status = "WRONG_PROJECT_NAME";
            error.description = "The project name doesn't match the specifications";
            send(error);
        } else if (!isValidProjectDescription(projectDescription)) {
            ErrorResponse error = new ErrorResponse();
            error.status = "WRONG_PROJECT_DESCRIPTION";
            error.description = "The project description doesn't match the specifications";
            send(error);
        } else {
            Project newProject = new Project();
            try {
                newProject.image = new URL("https://www.myfloridacfo.com/division/oit/images/DIS-HomeResponse.png");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            newProject.creationDate = LocalDateTime.now().withNano(0);
            newProject.users = getAllExistingUserIDs(memberIDs);
            newProject.users.add(user.id);
            newProject.id = new ObjectId();
            newProject.name = projectName;
            newProject.description = projectDescription;
            DB.addProject(newProject);
            ProjectResponse pm = new ProjectResponse();
            pm.project = newProject;
            send(pm);
        }
    }
    
    
    private Set<ObjectId> getAllExistingUserIDs(final List<String> userIDs) {
        return userIDs.parallelStream()
                .filter(ObjectId::isValid)
                .map(DB::getUserByUsernameOrEmail)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(u -> u.id)
                .collect(Collectors.toSet());
    }
    
    private void handleGetChatLogic(User user) {
        GetChatMessage getChatMessage = Serialisation.getGSON().fromJson(getLastMessage(), GetChatMessage.class);
        if (!ObjectId.isValid(getChatMessage.getPartnerUserID())) {
            ErrorResponse error = new ErrorResponse();
            error.status = "WRONG_USER_ID";
            error.description = "The specified userID doesn't conform to the right syntax";
            send(error);
        } else {
            ObjectId partnerID = new ObjectId(getChatMessage.getPartnerUserID());
            Optional<Chat> chat = DB.getChatByPartnerID(user.id, partnerID);
            Optional<User> partner = DB.getUser(partnerID);
            if (chat.isPresent() && partner.isPresent()) {
                ChatResponse cm = new ChatResponse();
                cm.chat = chat.get();
                cm.chat.partnerUsername = partner.get().username;
                send(cm);
            } else {
                if (partner.isEmpty()) {
                    ErrorResponse error = new ErrorResponse();
                    error.status = "NO_USER_FOUND";
                    error.description = "No user with the specified id was found";
                    send(error);
                } else {
                    Chat newChat = new Chat();
                    newChat.creationDate = LocalDateTime.now().withNano(0);
                    newChat.messages = Set.of();
                    newChat.personA = user.id;
                    newChat.personB = partner.get().id;
                    newChat.partnerUsername = partner.get().username;
                    newChat.id = new ObjectId();
                    DB.addChat(newChat);
                    ChatResponse cm = new ChatResponse();
                    cm.chat = newChat;
                    send(cm);
                }
            }
        }
    }
    
    private void handleGetHomeLogic(User user) {
        HomeResponse home = new HomeResponse();
        home.projects = DB.getProjectsForUser(user);
        home.chats = DB.getNLastChatsForUser(user.id, Properties.getUserConfig().amountfavouritechats());
        for (Chat chat : home.chats) {
            if (Objects.equals(chat.personA, user.id)) {//I am user a
                chat.partnerUsername = DB.getUser(chat.personB).map(u -> u.username).orElse("ERROR");
            } else {
                chat.partnerUsername = DB.getUser(chat.personA).map(u -> u.username).orElse("ERROR");
            }
        }
        send(home);
    }
    
    private void handleLogoutLogic(User user, String sessionID) {
        Optional<String> optionalDeviceID = LoginSessions.getDeviceID(sessionID);
        if (optionalDeviceID.isEmpty()) {
            //to send a internal server error, this should never happen
            ErrorResponse error = new ErrorResponse();
            error.status = "DEVICE_ID_NOT_FOUND";
            error.description = "The device-id of this session could not be not found";
            send(error);
            return;
        }
        String deviceID = optionalDeviceID.get();
        DB.removeSessionID(user, sessionID);
        DB.removeDeviceID(user, deviceID);
        sendSync(new LogoutResponse());
        LoginSessions.removeSession(sessionID);
        
    }
    
    private void handleGetUserLogic() {
        GetUserMessage getUserMessage = Serialisation.getGSON().fromJson(getLastMessage(), GetUserMessage.class);
        String userID = getUserMessage.getUserID();
        Optional<User> optionalUser;
        if (!ObjectId.isValid(userID)) {
            ErrorResponse error = new ErrorResponse();
            error.status = "NO_USER_FOUND";
            error.description = "No user with the specified id was found";
            send(error);
            return;
        } else if ((optionalUser = DB.getUser(new ObjectId(userID))).isEmpty()) {
            ErrorResponse error = new ErrorResponse();
            error.status = "NO_USER_FOUND";
            error.description = "No user with the specified id was found";
            send(error);
            return;
        }
        UserResponse ur = new UserResponse();
        ur.user = optionalUser.get();
        send(ur);
    }
    
    private void handleModifyProjectUsersLogic(User user) {
        ModifyProjectUsersMessage modifyProjectUsersMessage = Serialisation.getGSON().fromJson(getLastMessage(), ModifyProjectUsersMessage.class);
        Optional<User> optionalUser;
        Optional<Project> optionalProject;
        if (!ObjectId.isValid(modifyProjectUsersMessage.getUserID())
                || (optionalUser = DB.getUser(new ObjectId(modifyProjectUsersMessage.getUserID()))).isEmpty()) {
            ErrorResponse error = new ErrorResponse();
            error.status = "NO_USER_FOUND";
            error.description = "No user with the specified id was found";
            send(error);
            return;
        }
        if (!ObjectId.isValid(modifyProjectUsersMessage.getProjectID())
                || (optionalProject = DB.getProject(new ObjectId(modifyProjectUsersMessage.getProjectID()))).isEmpty()) {
            ErrorResponse error = new ErrorResponse();
            error.status = "NO_PROJECT_FOUND";
            error.description = "No project with the specified id was found";
            send(error);
            return;
        }
        User userToModify = optionalUser.get();
        Project projectToModify = optionalProject.get();
        if (modifyProjectUsersMessage.isAddUser()) {
            DB.addUserToProject(projectToModify, userToModify);
        } else {
            DB.removeUserFromProject(projectToModify, userToModify);
        }
    }
    
    private boolean isValidProjectDescription(String projectDescription) {
        return projectDescription.length() > 0 && projectDescription.length() < 200;
    }
    
    private Optional<Message> fillMessage(AddMessageMessage messageFromClient, User sender, boolean isChat) {
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
    
    private class HomeResponse {
        @Expose
        private final String status = "OK_HOME";
        @Expose
        Set<Project> projects;
        @Expose
        Set<Chat> chats;
    }
    
    class ChatResponse {
        @Expose
        private final String status = "OK_CHAT";
        @Expose
        Chat chat;
    }
    
    class ProjectResponse {
        @Expose
        private final String status = "OK_PROJECT";
        @Expose
        Project project;
    }
    
    class UsersResponse {
        @Expose
        private final String status = "OK_USERS";
        @Expose
        Set<User> users;
    }
    
    private class LogoutResponse {
        @Expose
        private final String status = "OK_LOGOUT";
    }
    
    class UserResponse {
        @Expose
        private final String status = "OK_USER";
        @Expose
        User user;
    }
    
    private class ErrorResponse {
        @Expose
        String status;
        @Expose
        String description;
    }
}
