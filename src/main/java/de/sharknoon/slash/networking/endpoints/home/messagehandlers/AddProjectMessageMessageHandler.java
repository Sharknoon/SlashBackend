package de.sharknoon.slash.networking.endpoints.home.messagehandlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.Project;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.database.models.message.Message;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.Status;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.ErrorResponse;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.ProjectResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.AddProjectMessageMessage;
import de.sharknoon.slash.networking.endpoints.home.messages.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.pushy.PushStatus;
import de.sharknoon.slash.networking.pushy.Pushy;
import de.sharknoon.slash.networking.sessions.LoginSessions;
import de.sharknoon.slash.serialisation.Serialisation;
import org.bson.types.ObjectId;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class AddProjectMessageMessageHandler extends HomeEndpointMessageHandler {

    public AddProjectMessageMessageHandler(HomeEndpoint homeEndpoint) {
        super(Status.ADD_PROJECT_MESSAGE, homeEndpoint);
    }

    public AddProjectMessageMessageHandler(HomeEndpoint homeEndpoint, HomeEndpointMessageHandler successor) {
        super(Status.ADD_PROJECT_MESSAGE, homeEndpoint, successor);
    }

    @Override
    public void messageLogic(StatusAndSessionIDMessage message, User user) {
        AddProjectMessageMessage addProjectMessageMessage = Serialisation.getGSON().fromJson(homeEndpoint.getLastMessage(), AddProjectMessageMessage.class);
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
            Message message1 = optionalMessage.get();
            Project p = project.get();
            DB.addMessageToProject(p, message1);
            //Project specific, send to every user of the project
            ProjectResponse pr = new ProjectResponse();
            pr.project = p;
            LoginSessions.getSessions(HomeEndpoint.class, p.usernames).forEach(session -> Endpoint.sendTo(session, pr));
            //Dont want to send the push notification to myself
            Set<User> usersWithoutSender = new HashSet<>(p.usernames);
            usersWithoutSender.remove(user);
            Pushy.sendPush(PushStatus.NEW_PROJECT_MESSAGE, message1, user.username, usersWithoutSender);
        }
    }
}
