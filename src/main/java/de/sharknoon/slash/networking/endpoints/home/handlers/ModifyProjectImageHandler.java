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
import de.sharknoon.slash.networking.endpoints.home.handlers.response.OKResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.ModifyProjectImageMessage;
import de.sharknoon.slash.serialisation.Serialisation;
import org.bson.types.ObjectId;

import java.util.Optional;

public class ModifyProjectImageHandler extends HomeEndpointHandler {

    public ModifyProjectImageHandler(HomeEndpoint homeEndpoint) {
        super(Status.MODIFY_PROJECT_IMAGE, homeEndpoint);
    }

    public ModifyProjectImageHandler(HomeEndpoint homeEndpoint, HomeEndpointHandler successor) {
        super(Status.MODIFY_PROJECT_IMAGE, homeEndpoint, successor);
    }

    @Override
    protected void messageLogic(StatusAndSessionIDMessage message, User user) {
        ModifyProjectImageMessage modifyProjectImageMessage = Serialisation.getGSON().fromJson(homeEndpoint.getLastTextMessage(), ModifyProjectImageMessage.class);
        Optional<Project> optionalProject = Optional.empty();
        if (!ObjectId.isValid(modifyProjectImageMessage.getProjectID())
                || (optionalProject = DB.getProject(new ObjectId(modifyProjectImageMessage.getProjectID()))).isEmpty()) {
            ErrorResponse error = new ErrorResponse();
            error.status = "NO_PROJECT_FOUND";
            error.description = "No project with the specified id was found";
            homeEndpoint.send(error);
            return;
        }
        if (modifyProjectImageMessage.isRemoved()) {
            DB.removeImage(optionalProject.get());
            OKResponse response = new OKResponse();
            homeEndpoint.send(response);
        } else {
            //Constructing new ID for the future image to be uploaded
            ObjectId newImageObjectID = new ObjectId();
            String newImageID = newImageObjectID.toHexString();
            ImageResponse ir = new ImageResponse();
            ir.imageID = newImageID;
            //Adding the id for allowing upload access
            FileEndpoint.allowUpload(newImageID);
            //Sending the imageID to the user to allow for the upload
            homeEndpoint.send(ir);
            user.image = newImageObjectID;
            DB.setImage(optionalProject.get(), newImageObjectID);
        }
    }
}
