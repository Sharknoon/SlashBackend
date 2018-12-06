package de.sharknoon.slash.networking.endpoints.home.messagehandlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.*;
import de.sharknoon.slash.networking.endpoints.home.*;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.*;
import de.sharknoon.slash.networking.endpoints.home.messages.*;
import de.sharknoon.slash.serialisation.Serialisation;
import org.bson.types.ObjectId;

import java.util.Optional;

public class GetProjectMessageHandler extends HomeEndpointMessageHandler {

    public GetProjectMessageHandler(HomeEndpoint homeEndpoint) {
        super(Status.GET_PROJECT, homeEndpoint);
    }

    public GetProjectMessageHandler(HomeEndpoint homeEndpoint, HomeEndpointMessageHandler successor) {
        super(Status.GET_PROJECT, homeEndpoint, successor);
    }

    @Override
    public void messageLogic(StatusAndSessionIDMessage message, User user) {
        GetProjectMessage getProjectMessage = Serialisation.getGSON().fromJson(homeEndpoint.getLastTextMessage(), GetProjectMessage.class);
        if (!ObjectId.isValid(getProjectMessage.getProjectID())) {
            ErrorResponse error = new ErrorResponse();
            error.status = "WRONG_PROJECT_ID";
            error.description = "The specified projectID doesn't conform to the right syntax";
            homeEndpoint.send(error);
        } else {
            ObjectId projectID = new ObjectId(getProjectMessage.getProjectID());
            Optional<Project> project = DB.getProject(projectID);
            if (project.isPresent()) {
                ProjectResponse pm = new ProjectResponse();
                pm.project = project.get();
                homeEndpoint.send(pm);
            } else {
                ErrorResponse error = new ErrorResponse();
                error.status = "NO_PROJECT_FOUND";
                error.description = "No project with the specified id was found";
                homeEndpoint.send(error);
            }
        }
    }
}
