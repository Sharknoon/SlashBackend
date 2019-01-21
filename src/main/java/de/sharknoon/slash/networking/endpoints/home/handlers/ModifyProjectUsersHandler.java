package de.sharknoon.slash.networking.endpoints.home.handlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.Project;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.handlers.response.ErrorResponse;
import de.sharknoon.slash.networking.endpoints.home.handlers.response.OKResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.ModifyProjectUsersMessage;
import de.sharknoon.slash.serialisation.Serialisation;
import org.bson.types.ObjectId;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class ModifyProjectUsersHandler extends HomeEndpointHandler {

    public ModifyProjectUsersHandler(HomeEndpoint homeEndpoint) {
        super(Status.MODIFY_PROJECT_USERS, homeEndpoint);
    }

    public ModifyProjectUsersHandler(HomeEndpoint homeEndpoint, HomeEndpointHandler successor) {
        super(Status.MODIFY_PROJECT_USERS, homeEndpoint, successor);
    }

    @Override
    protected void messageLogic(StatusAndSessionIDMessage message, User user) {
        ModifyProjectUsersMessage modifyProjectUsersMessage = Serialisation.getGSON().fromJson(homeEndpoint.getLastTextMessage(), ModifyProjectUsersMessage.class);
        Optional<Project> optionalProject;
        if (!ObjectId.isValid(modifyProjectUsersMessage.getProjectID())
                || (optionalProject = DB.getProject(new ObjectId(modifyProjectUsersMessage.getProjectID()))).isEmpty()) {
            ErrorResponse error = new ErrorResponse();
            error.status = "NO_PROJECT_FOUND";
            error.description = "No project with the specified id was found";
            homeEndpoint.send(error);
        } else {
            Set<String> usersIDs = modifyProjectUsersMessage.getUsers();
            Set<User> users = usersIDs.stream()
                    .filter(ObjectId::isValid)
                    .map(ObjectId::new)
                    .map(DB::getUser)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            Project project = optionalProject.get();
            if (modifyProjectUsersMessage.isAddUsers()) {
                DB.addUsersToProject(project, users);
            } else {
                DB.removeUsersFromProject(project, users);
                if (project.users.isEmpty()) {
                    DB.deleteProject(project);
                }
            }
            if (users.size() < usersIDs.size()) {
                ErrorResponse error = new ErrorResponse();
                error.status = "NO_USER_FOUND";
                error.description = "No user with the specified id was found";
                homeEndpoint.send(error);
            } else {
                OKResponse response = new OKResponse();
                homeEndpoint.send(response);
            }
        }
    }
}
