package de.sharknoon.slash.networking.endpoints.home;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.*;
import de.sharknoon.slash.networking.LoginSessions;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import de.sharknoon.slash.properties.Properties;
import org.bson.types.ObjectId;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.time.LocalDateTime;
import java.util.*;

@ServerEndpoint("/home")
public class HomeEndpoint extends Endpoint<HomeMessage> {
    
    public HomeEndpoint() {
        super(HomeMessage.class);
    }
    
    private static final String GET_HOME_STATUS = "GET_HOME";
    private static final String GET_CHAT_STATUS = "GET_CHAT";
    private static final String ADD_PROJECT_STATUS = "ADD_PROJECT";
    private static final String GET_PROJECT_STATUS = "GET_PROJECT";
    private static final String GET_USER = "GET_USER";

    @Override
    protected void onMessage(Session session, HomeMessage message) {
        Optional<User> user = LoginSessions.getUser(message.getSessionid());
        
        String returnMessage;
        
        if (!user.isPresent()) {//To be replaced with isEmpty, this is because intellij shows a warning because it doesnt know the new isEmtpy()
            returnMessage = "{\"status\":\"NO_LOGIN_OR_TOO_MUCH_DEVICES\"," +
                    "\"message\":\"You are either not logged in or using more than " +
                    Properties.getUserConfig().maxdevices() + " devices\"}";
        } else {
            Object messageToReturn = getReturnMessage(message, user.get());
            try {
                returnMessage = GSON.toJson(messageToReturn);
            } catch (Exception e) {
                returnMessage = "{\"status\":\"ERROR\"," +
                        "\"message\":\"An unexpected error occurred, please try again later\"}";
            }
        }
        
        session.getAsyncRemote().sendText(returnMessage);
    }
    
    private Object getReturnMessage(HomeMessage message, User user) {
        switch (message.getStatus()) {
            case GET_HOME_STATUS:
                Home home = new Home();
                home.projects = DB.getProjectsForUser(user);
                home.chats = DB.getNLastChatsForUser(user, Properties.getUserConfig().amountfavouritechats());
                return home;
            case GET_CHAT_STATUS:
                if (!ObjectId.isValid(message.getPartnerUserID())) {
                    Error error = new Error();
                    error.status = "WRONG_USER_ID";
                    error.description = "The specified userID doesn't conform to the right syntax";
                    return error;
                } else {
                    ObjectId partnerID = new ObjectId(message.getPartnerUserID());
                    Optional<Chat> chat = DB.getChatByPartnerID(partnerID);
                    if (chat.isPresent()) {
                        return chat.get();
                    } else {
                        if (!DB.existsUserID(partnerID)) {
                            Error error = new Error();
                            error.status = "NO_USER_FOUND";
                            error.description = "No user with the specified id was found";
                            return error;
                        } else {
                            Chat newChat = new Chat();
                            newChat.creationDate = LocalDateTime.now();
                            newChat.messages = List.of();
                            newChat.personA = user.id;
                            newChat.personB = partnerID;
                            newChat.id = new ObjectId();
                            DB.addChat(newChat);
                            return newChat;
                        }
                    }
                }
            case ADD_PROJECT_STATUS:
                String projectName = message.getProjectName();
                if (!checkProjectName(projectName)) {
                    Error error = new Error();
                    error.status = "WRONG_PROJECT_NAME";
                    error.description = "The project name doesn't match the specifications";
                    return error;
                } else {
                    Project newProject = new Project();
                    newProject.image = "https://www.myfloridacfo.com/division/oit/images/DIS-Home.png";
                    newProject.creationDate = LocalDateTime.now();
                    newProject.users = Set.of(user.id);
                    newProject.id = new ObjectId();
                    newProject.name = projectName;
                    DB.addProject(newProject);
                    return newProject;
                }
            case GET_PROJECT_STATUS:
                if (!ObjectId.isValid(message.getProjectID())) {
                    Error error = new Error();
                    error.status = "WRONG_PROJECT_ID";
                    error.description = "The specified projectID doesn't conform to the right syntax";
                    return error;
                } else {
                    ObjectId projectID = new ObjectId(message.getProjectID());
                    Optional<Project> project = DB.getProject(projectID);
                    if (project.isPresent()) {
                        return project.get();
                    } else {
                        Error error = new Error();
                        error.status = "NO_PROJECT_FOUND";
                        error.description = "No project with the specified id was found";
                        return error;
                    }
                }
            case GET_USER:
                Optional<User> userForUsername = DB.getUserForUsername(message.getUsername());
                if (userForUsername.isPresent()) {
                    User user2 = userForUsername.get();
                    UserID id = new UserID();
                    id.username = user2.username;
                    id.userID = user2.id.toString();
                    return id;
                } else {
                    Error error = new Error();
                    error.status = "NO_USER_FOUND";
                    error.description = "No user with the specified username was found";
                    return error;
                }
            default:
                Error error = new Error();
                error.status = "WRONG_STATUS";
                error.description = "The status was wrong, please check the API";
                return error;
        }
    }
    
    private boolean checkProjectName(String projectName) {
        return projectName.length() > 0 && projectName.length() < 20;
    }
    
    private class Home {
        @Expose
        Set<Project> projects;
        @Expose
        Set<Chat> chats;
    }
    
    private class UserID {
        @Expose
        String username;
        @Expose
        String userID;
    }
    
    private class Error {
        @Expose
        String status;
        @Expose
        String description;
    }
}
