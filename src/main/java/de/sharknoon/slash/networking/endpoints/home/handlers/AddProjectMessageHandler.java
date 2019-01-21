package de.sharknoon.slash.networking.endpoints.home.handlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.Project;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.database.models.message.Message;
import de.sharknoon.slash.networking.apis.pushy.PushStatus;
import de.sharknoon.slash.networking.apis.pushy.Pushy;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.handlers.response.ErrorResponse;
import de.sharknoon.slash.networking.endpoints.home.handlers.response.ProjectResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.AddProjectMessageMessage;
import de.sharknoon.slash.networking.sessions.LoginSessions;
import de.sharknoon.slash.serialisation.Serialisation;
import org.bson.types.ObjectId;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class AddProjectMessageHandler extends HomeEndpointHandler {

    public AddProjectMessageHandler(HomeEndpoint homeEndpoint) {
        super(Status.ADD_PROJECT_MESSAGE, homeEndpoint);
    }

    public AddProjectMessageHandler(HomeEndpoint homeEndpoint, HomeEndpointHandler successor) {
        super(Status.ADD_PROJECT_MESSAGE, homeEndpoint, successor);
    }

    @Override
    public void messageLogic(StatusAndSessionIDMessage statusAndSessionID, User user) {
        AddProjectMessageMessage addProjectMessageMessage = Serialisation.getGSON().fromJson(homeEndpoint.getLastTextMessage(), AddProjectMessageMessage.class);
        Optional<Project> optionalProject;
        if (!ObjectId.isValid(addProjectMessageMessage.getProjectID())) {
            //wrong chat id syntax
            ErrorResponse error = new ErrorResponse();
            error.status = "WRONG_PROJECT_ID";
            error.description = "The syntax of the project-ID was not correct";
            homeEndpoint.send(error);
        } else if ((optionalProject = DB.getProject(new ObjectId(addProjectMessageMessage.getProjectID()))).isEmpty()) {
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
            Message message = optionalMessage.get();
            Project project = optionalProject.get();
            DB.addMessageToProject(project, message);
            //Project specific, send to every user of the project
            ProjectResponse pr = new ProjectResponse();
            pr.project = project;
            LoginSessions.getSessions(HomeEndpoint.class, project.usernames).forEach(session -> homeEndpoint.sendTo(session, pr));
            //Dont want to send the push notification to myself
            Set<User> usersWithoutSender = new HashSet<>(project.usernames);
            usersWithoutSender.remove(user);
            Pushy.sendMessagePush(PushStatus.NEW_PROJECT_MESSAGE, project.id.toHexString(), message, project.name + ": " + user.username, usersWithoutSender);
        }
    }
}
