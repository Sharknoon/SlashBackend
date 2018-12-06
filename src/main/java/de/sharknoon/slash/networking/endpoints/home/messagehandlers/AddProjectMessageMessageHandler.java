package de.sharknoon.slash.networking.endpoints.home.messagehandlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.*;
import de.sharknoon.slash.database.models.message.Message;
import de.sharknoon.slash.networking.endpoints.home.*;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.*;
import de.sharknoon.slash.networking.endpoints.home.messages.*;
import de.sharknoon.slash.networking.pushy.*;
import de.sharknoon.slash.networking.sessions.LoginSessions;
import de.sharknoon.slash.serialisation.Serialisation;
import org.bson.types.ObjectId;

import java.util.*;

public class AddProjectMessageMessageHandler extends HomeEndpointMessageHandler {
    
    public AddProjectMessageMessageHandler(HomeEndpoint homeEndpoint) {
        super(Status.ADD_PROJECT_MESSAGE, homeEndpoint);
    }
    
    public AddProjectMessageMessageHandler(HomeEndpoint homeEndpoint, HomeEndpointMessageHandler successor) {
        super(Status.ADD_PROJECT_MESSAGE, homeEndpoint, successor);
    }
    
    @Override
    public void messageLogic(StatusAndSessionIDMessage message, User user) {
        AddProjectMessageMessage addProjectMessageMessage = Serialisation.getGSON().fromJson(homeEndpoint.getLastTextMessage(), AddProjectMessageMessage.class);
        Optional<Project> project;
        if (!ObjectId.isValid(addProjectMessageMessage.getProjectID())) {
            //wrong chat id syntax
            ErrorResponse error = new ErrorResponse();
            error.status = "WRONG_PROJECT_ID";
            error.description = "The syntax of the project-ID was not correct";
            homeEndpoint.send(error);
        } else if ((project = DB.getProject(new ObjectId(addProjectMessageMessage.getProjectID()))).isEmpty()) {
            //chat id doesn't exist
            ErrorResponse error = new ErrorResponse();
            error.status = "NO_PROJECT_FOUND";
            error.description = "No corresponding project to the project-ID was found";
            homeEndpoint.send(error);
        } else {
            Optional<Message> optionalMessage = homeEndpoint.fillMessage(addProjectMessageMessage, user, false);
            //Errors already send
            if (optionalMessage.isEmpty()) {
                return;
            }
            Message m = optionalMessage.get();
            Project p = project.get();
            DB.addMessageToProject(p, m);
            //Project specific, send to every user of the project
            ProjectResponse pr = new ProjectResponse();
            pr.project = p;
            LoginSessions.getSessions(HomeEndpoint.class, p.usernames).forEach(session -> homeEndpoint.sendTo(session, pr));
            //Dont want to send the push notification to myself
            Set<User> usersWithoutSender = new HashSet<>(p.usernames);
            usersWithoutSender.remove(user);
            Pushy.sendPush(PushStatus.NEW_PROJECT_MESSAGE, p.id.toHexString(), m, user.username, usersWithoutSender);
        }
    }
}
