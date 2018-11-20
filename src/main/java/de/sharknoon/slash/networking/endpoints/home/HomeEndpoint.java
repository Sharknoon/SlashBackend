package de.sharknoon.slash.networking.endpoints.home;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.Chat;
import de.sharknoon.slash.database.models.Project;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.LoginSessions;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import de.sharknoon.slash.properties.Properties;
import org.bson.types.ObjectId;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@ServerEndpoint("/home")
public class HomeEndpoint extends Endpoint<HomeMessage> {

    public static final String GET_USER_STATUS = "GET_USER";
    public static final String GET_HOME_STATUS = "GET_HOME";
    public static final String GET_CHAT_STATUS = "GET_CHAT";
    public static final String ADD_PROJECT_STATUS = "ADD_PROJECT";
    public static final String GET_PROJECT_STATUS = "GET_PROJECT";
    public static final String ADD_MESSAGE_STATUS = "ADD_MESSAGE";
    
    private static boolean isValidChatMessage(String message) {
        return message.length() < 5000;
    }
    
    //Needs to stay public
    @SuppressWarnings("WeakerAccess")
    public HomeEndpoint() {
        super(HomeMessage.class);
    }
    
    @Override
    protected void onMessage(Session session, HomeMessage message) {
        Optional<User> user = LoginSessions.getUser(message.getSessionid());
        
        if (!user.isPresent()) {//To be replaced with isEmpty, this is because intellij shows a warning because it doesnt know the new isEmtpy()
            send("{\"status\":\"NO_LOGIN_OR_TOO_MUCH_DEVICES\"," +
                    "\"message\":\"You are either not logged in or using more than " +
                    Properties.getUserConfig().maxdevices() + " devices\"}");
        } else {
            LoginSessions.addSession(user.get(), message.getSessionid(), HomeEndpoint.class, session);
            handleLogic(message, user.get());
        }
    }
    
    private void handleLogic(HomeMessage message, User user) {
        switch (message.getStatus()) {
            case GET_HOME_STATUS:
                HomeResponse home = new HomeResponse();
                home.projects = DB.getProjectsForUser(user);
                home.chats = DB.getNLastChatsForUser(user.id, Properties.getUserConfig().amountfavouritechats());
                //TMP
                for (Chat chat : home.chats) {
                    if (Objects.equals(chat.personA, user.id)) {//I am user a
                        chat.partnerUsername = DB.getUser(chat.personB).map(u -> u.username).orElse("ERROR");
                    } else {
                        chat.partnerUsername = DB.getUser(chat.personA).map(u -> u.username).orElse("ERROR");
                    }
                }
                send(home);
                break;
            case GET_CHAT_STATUS:
                if (!ObjectId.isValid(message.getPartnerUserID())) {
                    ErrorResponse error = new ErrorResponse();
                    error.status = "WRONG_USER_ID";
                    error.description = "The specified userID doesn't conform to the right syntax";
                    send(error);
                } else {
                    ObjectId partnerID = new ObjectId(message.getPartnerUserID());
                    Optional<Chat> chat = DB.getChatByPartnerID(user.id, partnerID);
                    Optional<User> partner = DB.getUser(partnerID);
                    if (chat.isPresent() && partner.isPresent()) {
                        ChatResponse cm = new ChatResponse();
                        cm.chat = chat.get();
                        cm.chat.partnerUsername = partner.get().username;
                        send(cm);
                    } else {
                        if (!partner.isPresent()) {
                            ErrorResponse error = new ErrorResponse();
                            error.status = "NO_USER_FOUND";
                            error.description = "No user with the specified id was found";
                            send(error);
                        } else {
                            Chat newChat = new Chat();
                            newChat.creationDate = LocalDateTime.now();
                            newChat.messages = List.of();
                            newChat.personA = user.id;
                            //newChat.nameA = user.username;
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
                break;
            case ADD_PROJECT_STATUS:
                String projectName = message.getProjectName();
                if (!isValidProjectName(projectName)) {
                    ErrorResponse error = new ErrorResponse();
                    error.status = "WRONG_PROJECT_NAME";
                    error.description = "The project name doesn't match the specifications";
                    send(error);
                } else {
                    Project newProject = new Project();
                    newProject.image = "https://www.myfloridacfo.com/division/oit/images/DIS-HomeResponse.png";
                    newProject.creationDate = LocalDateTime.now();
                    newProject.users = Set.of(user.id);
                    newProject.id = new ObjectId();
                    newProject.name = projectName;
                    DB.addProject(newProject);
                    ProjectResponse pm = new ProjectResponse();
                    pm.project = newProject;
                    send(pm);
                }
                break;
            case GET_PROJECT_STATUS:
                if (!ObjectId.isValid(message.getProjectID())) {
                    ErrorResponse error = new ErrorResponse();
                    error.status = "WRONG_PROJECT_ID";
                    error.description = "The specified projectID doesn't conform to the right syntax";
                    send(error);
                } else {
                    ObjectId projectID = new ObjectId(message.getProjectID());
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
                break;
            case GET_USER_STATUS:
                Optional<User> userForUsername = DB.getUserForUsername(message.getUsername());
                if (userForUsername.isPresent()) {
                    UserResponse um = new UserResponse();
                    um.user = userForUsername.get();
                    send(um);
                } else {
                    ErrorResponse error = new ErrorResponse();
                    error.status = "NO_USER_FOUND";
                    error.description = "No user with the specified username was found";
                    send(error);
                }
                break;
            case ADD_MESSAGE_STATUS:
                String chatMessage = message.getMessage();
                if (isValidChatMessage(chatMessage)) {
                    if (ObjectId.isValid(message.getChatID())) {
                        Optional<Chat> chat = DB.getChat(new ObjectId(message.getChatID()));
                        if (chat.isPresent()) {
                            Chat c = chat.get();
                            Optional<User> partner;
                            if (Objects.equals(c.personA, user.id)) {//I am user a
                                partner = DB.getUser(c.personB);
                            } else {
                                partner = DB.getUser(c.personA);
                            }
                            if (partner.isPresent()) {
                                DB.addMessageToChat(c, chatMessage);
                                ChatResponse cr = new ChatResponse();
                                cr.chat = c;
                                c.partnerUsername = partner.get().username;
                                String chatResponseToMe = toJSON(cr);
                                LoginSessions.getSession(HomeEndpoint.class, user)
                                        .ifPresent(session -> session.getAsyncRemote().sendText(chatResponseToMe));
                                c.partnerUsername = user.username;
                                String chatResponseToPartner = toJSON(cr);
                                LoginSessions.getSession(HomeEndpoint.class, partner.get())
                                        .ifPresent(session -> session.getAsyncRemote().sendText(chatResponseToPartner));
                            } else {
                                //partner doesn't exists
                                ErrorResponse error = new ErrorResponse();
                                error.status = "CHAT_PARTNER_NOT_FOUND";
                                error.description = "No corresponding chat partner to the chat-ID was found";
                                send(error);
                            }
                        } else {
                            //chat id doesnt exist
                            ErrorResponse error = new ErrorResponse();
                            error.status = "NO_CHAT_FOUND";
                            error.description = "No corresponding chat to the chat-ID was found";
                            send(error);
                        }
                    } else {
                        //wrong chat id syntax
                        ErrorResponse error = new ErrorResponse();
                        error.status = "WRONG_CHAT_ID";
                        error.description = "The syntax of the chat-ID was not correct";
                        send(error);
                    }
                } else {
                    //Chat message malformed
                    ErrorResponse error = new ErrorResponse();
                    error.status = "CHAT_MESSAGE_TOO_LONG";
                    error.description = "The chat message was over 5000 characters long";
                    send(error);
                }
                break;
            default:
                ErrorResponse error = new ErrorResponse();
                error.status = "WRONG_STATUS";
                error.description = "The status was wrong, please check the API";
                send(error);
        }
    }
    
    private boolean isValidProjectName(String projectName) {
        return projectName.length() > 0 && projectName.length() < 20;
    }
    
    private class HomeResponse {
        @Expose
        String status = "OK_HOME";
        @Expose
        Set<Project> projects;
        @Expose
        Set<Chat> chats;
    }
    
    private class ChatResponse {
        @Expose
        String status = "OK_CHAT";
        @Expose
        Chat chat;
    }
    
    private class ProjectResponse {
        @Expose
        String status = "OK_PROJECT";
        @Expose
        Project project;
    }
    
    class UserResponse {
        @Expose
        String status = "OK_USER";
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
