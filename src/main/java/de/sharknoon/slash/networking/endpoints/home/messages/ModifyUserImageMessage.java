package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;

public class ModifyUserImageMessage extends StatusAndSessionIDMessage {

    @Expose
    private boolean remove = false;

    public ModifyUserImageMessage() {
        super(Status.MODIFY_USER_IMAGE);
    }

    public boolean isRemoved() {
        return remove;
    }

    public void setRemoved(boolean removed) {
        this.remove = removed;
    }

}
