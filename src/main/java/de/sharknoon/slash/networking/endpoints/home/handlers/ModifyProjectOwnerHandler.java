package de.sharknoon.slash.networking.endpoints.home.handlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.Project;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.handlers.response.ErrorResponse;
import de.sharknoon.slash.networking.endpoints.home.handlers.response.OKResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.ModifyProjectOwnerMessage;
import de.sharknoon.slash.serialisation.Serialisation;
import org.bson.types.ObjectId;

import java.util.Optional;

public class ModifyProjectOwnerHandler extends HomeEndpointHandler {

    public ModifyProjectOwnerHandler(HomeEndpoint homeEndpoint) {
        super(Status.MODIFY_PROJECT_OWNER, homeEndpoint);
    }

    public ModifyProjectOwnerHandler(HomeEndpoint homeEndpoint, HomeEndpointHandler successor) {
        super(Status.MODIFY_PROJECT_OWNER, homeEndpoint, successor);
    }

    @Override
    protected void messageLogic(StatusAndSessionIDMessage message, User user) {
        ModifyProjectOwnerMessage modifyProjectUsersMessage = Serialisation.getGSON().fromJson(homeEndpoint.getLastTextMessage(), ModifyProjectOwnerMessage.class);
        Optional<User> optionalUser = Optional.empty();
        Optional<Project> optionalProject;
        if (!modifyProjectUsersMessage.getProjectOwner().isEmpty() && !ObjectId.isValid(modifyProjectUsersMessage.getProjectOwner())
                || (!modifyProjectUsersMessage.getProjectOwner().isEmpty() && (optionalUser = DB.getUser(new ObjectId(modifyProjectUsersMessage.getProjectOwner()))).isEmpty())) {
            ErrorResponse error = new ErrorResponse();
            error.status = "NO_USER_FOUND";
            error.description = "No user with the specified id was found";
            homeEndpoint.send(error);
        } else if (!ObjectId.isValid(modifyProjectUsersMessage.getProjectID())
                || (optionalProject = DB.getProject(new ObjectId(modifyProjectUsersMessage.getProjectID()))).isEmpty()) {
            ErrorResponse error = new ErrorResponse();
            error.status = "NO_PROJECT_FOUND";
            error.description = "No project with the specified id was found";
            homeEndpoint.send(error);
        } else {
            User userToModify = optionalUser.orElse(null);
            Project projectToModify = optionalProject.get();
            DB.setProjectOwner(projectToModify, userToModify);
            OKResponse response = new OKResponse();
            homeEndpoint.send(response);
        }
    }
}
