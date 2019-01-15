package de.sharknoon.slash.networking.endpoints.home.handlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.Project;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.endpoints.file.FileEndpoint;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.handlers.response.ErrorResponse;
import de.sharknoon.slash.networking.endpoints.home.handlers.response.ImageResponse;
import de.sharknoon.slash.networking.endpoints.home.handlers.response.ProjectResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.AddProjectMessage;
import de.sharknoon.slash.serialisation.Serialisation;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.List;

public class AddProjectHandler extends HomeEndpointHandler {

    public AddProjectHandler(HomeEndpoint homeEndpoint) {
        super(Status.ADD_PROJECT, homeEndpoint);
    }

    public AddProjectHandler(HomeEndpoint homeEndpoint, HomeEndpointHandler successor) {
        super(Status.ADD_PROJECT, homeEndpoint, successor);
    }

    @Override
    public void messageLogic(StatusAndSessionIDMessage message, User user) {
        AddProjectMessage addProjectMessage = Serialisation.getGSON().fromJson(homeEndpoint.getLastTextMessage(), AddProjectMessage.class);
        String projectName = addProjectMessage.getProjectName();
        String projectDescription = addProjectMessage.getProjectDescription();
        List<String> memberIDs = addProjectMessage.getProjectMembers();
        String projectOwner = addProjectMessage.getProjectOwner();
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
        } else if ((!projectOwner.isEmpty() && !ObjectId.isValid(projectOwner)) || (!projectOwner.isEmpty() && DB.getUser(new ObjectId(projectOwner)).isEmpty())) {
            ErrorResponse error = new ErrorResponse();
            error.status = "WRONG_PROJECT_OWNER";
            error.description = "The project owner is not a valid id";
            homeEndpoint.send(error);
        } else {
            Project newProject = new Project();
            newProject.creationDate = LocalDateTime.now().withNano(0);
            newProject.users = homeEndpoint.getAllExistingUserIDs(memberIDs);
            newProject.users.add(user.id);
            newProject.id = new ObjectId();
            newProject.name = projectName;
            newProject.description = projectDescription;
            //Optional
            if (!projectOwner.isEmpty()) {
                newProject.projectOwner = new ObjectId(projectOwner);
            }
            if (addProjectMessage.isWithProjectImage()) {
                // Constructing new ID for the project image to be uploaded
                final ObjectId newImageObjectID = new ObjectId();
                newProject.image = newImageObjectID;

                final String newImageID = newImageObjectID.toHexString();
                ImageResponse ir = new ImageResponse();
                ir.imageID = newImageID;
                FileEndpoint.allowUpload(newImageID);   // Adding the id for allowing upload access
                homeEndpoint.send(ir);                  // Sending the imageID to the user to allow for the upload
            }
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
        return projectDescription.length() < 200;
    }
}
