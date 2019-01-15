package de.sharknoon.slash.networking.endpoints.home.handlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.endpoints.file.FileEndpoint;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.handlers.response.ImageResponse;
import de.sharknoon.slash.networking.endpoints.home.handlers.response.OKResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.ModifyUserImageMessage;
import de.sharknoon.slash.serialisation.Serialisation;
import org.bson.types.ObjectId;

public class ModifyUserImageHandler extends HomeEndpointHandler {

    public ModifyUserImageHandler(HomeEndpoint homeEndpoint) {
        super(Status.MODIFY_USER_IMAGE, homeEndpoint);
    }

    public ModifyUserImageHandler(HomeEndpoint homeEndpoint, HomeEndpointHandler successor) {
        super(Status.MODIFY_USER_IMAGE, homeEndpoint, successor);
    }

    @Override
    protected void messageLogic(StatusAndSessionIDMessage message, User user) {
        ModifyUserImageMessage modifyUserImageMessage = Serialisation.getGSON().fromJson(homeEndpoint.getLastTextMessage(), ModifyUserImageMessage.class);
        if (modifyUserImageMessage.isRemoved()) {
            DB.removeImage(user);
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
            DB.setImage(user, newImageObjectID);
        }
    }
}
