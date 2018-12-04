package de.sharknoon.slash.networking.endpoints.home.messagehandlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.Chat;
import de.sharknoon.slash.database.models.Project;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.Status;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.ErrorResponse;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.HomeResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.ModifyProjectUsersMessage;
import de.sharknoon.slash.networking.endpoints.home.messages.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.sessions.LoginSessions;
import de.sharknoon.slash.properties.Properties;
import de.sharknoon.slash.serialisation.Serialisation;
import org.bson.types.ObjectId;

import javax.websocket.Session;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class ModifyProjectUsersMessageHandler extends HomeEndpointMessageHandler {
    public ModifyProjectUsersMessageHandler(HomeEndpoint homeEndpoint, HomeEndpointMessageHandler successor) {
        super(Status.MODIFY_PROJECT_USERS, homeEndpoint, successor);
    }

    @Override
    protected void messageLogic(StatusAndSessionIDMessage message, User user) {
        ModifyProjectUsersMessage modifyProjectUsersMessage = Serialisation.getGSON().fromJson(homeEndpoint.getLastMessage(), ModifyProjectUsersMessage.class);
        Optional<User> optionalUser;
        Optional<Project> optionalProject;
        if (!ObjectId.isValid(modifyProjectUsersMessage.getUserID())
                || (optionalUser = DB.getUser(new ObjectId(modifyProjectUsersMessage.getUserID()))).isEmpty()) {
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
            User userToModify = optionalUser.get();
            Project projectToModify = optionalProject.get();
            if (modifyProjectUsersMessage.isAddUser()) {
                DB.addUserToProject(projectToModify, userToModify);
            } else {
                DB.removeUserFromProject(projectToModify, userToModify);
            }

            LoginSessions.getSessionsWithUser(HomeEndpoint.class, Set.of(userToModify))
                    .forEach(e -> {
                        User u = e.getValue();
                        Session s = e.getKey();
                        HomeResponse home = new HomeResponse();
                        home.projects = DB.getProjectsForUser(u);
                        home.chats = DB.getNLastChatsForUser(u.id, Properties.getUserConfig().amountfavouritechats());
                        for (Chat c : home.chats) {
                            if (Objects.equals(c.personA, u.id)) {//I am user a
                                c.partnerUsername = DB.getUser(c.personB).map(usr -> usr.username).orElse("ERROR");
                            } else {
                                c.partnerUsername = DB.getUser(c.personA).map(usr -> usr.username).orElse("ERROR");
                            }
                        }
                        Endpoint.sendTo(s, home);
                    });

        }
    }
}
