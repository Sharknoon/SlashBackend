package de.sharknoon.slash.networking.endpoints.home.messagehandlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.Project;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.Status;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.ErrorResponse;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.ProjectResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.AddProjectMessage;
import de.sharknoon.slash.networking.endpoints.home.messages.StatusAndSessionIDMessage;
import de.sharknoon.slash.serialisation.Serialisation;
import org.bson.types.ObjectId;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

public class AddProjectMessageHandler extends HomeEndpointMessageHandler {
    public AddProjectMessageHandler(HomeEndpoint homeEndpoint, HomeEndpointMessageHandler successor) {
        super(Status.ADD_PROJECT, homeEndpoint, successor);
    }

    @Override
    public void messageLogic(StatusAndSessionIDMessage message, User user) {
        AddProjectMessage addProjectMessage = Serialisation.getGSON().fromJson(homeEndpoint.getLastMessage(), AddProjectMessage.class);
        String projectName = addProjectMessage.getProjectName();
        String projectDescription = addProjectMessage.getProjectDescription();
        List<String> memberIDs = addProjectMessage.getProjectMembers();
        if (!isValidProjectName(projectName)) {
            ErrorResponse error = new ErrorResponse();
            error.status = "WRONG_PROJECT_NAME";
            error.description = "The project name doesn't match the specifications";
            homeEndpoint.send(error);
        } else if (!isValidProjectDescription(projectDescription)) {
            ErrorResponse error = new ErrorResponse();
            error.status = "WRONG_PROJECT_DESCRIPTION";
            error.description = "The project description doesn't match the specifications";
            homeEndpoint.send(error);
        } else {
            Project newProject = new Project();
            try {
                newProject.image = new URL("https://www.myfloridacfo.com/division/oit/images/DIS-HomeResponse.png");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            newProject.creationDate = LocalDateTime.now().withNano(0);
            newProject.users = homeEndpoint.getAllExistingUserIDs(memberIDs);
            newProject.users.add(user.id);
            newProject.id = new ObjectId();
            newProject.name = projectName;
            newProject.description = projectDescription;
            DB.addProject(newProject);
            ProjectResponse pm = new ProjectResponse();
            pm.project = newProject;
            homeEndpoint.send(pm);
        }
    }

    private boolean isValidProjectName(String projectName) {
        return projectName.length() > 0 && projectName.length() < 20;
    }

    private boolean isValidProjectDescription(String projectDescription) {
        return projectDescription.length() > 0 && projectDescription.length() < 200;
    }
}