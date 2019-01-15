package de.sharknoon.slash.networking.endpoints.home.handlers;

import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;

public abstract class HomeEndpointHandler {

    private final Status messageStatus;
    protected final HomeEndpoint homeEndpoint;
    protected HomeEndpointHandler successor;


    public HomeEndpointHandler(final Status messageStatus, final HomeEndpoint homeEndpoint) {
        this(messageStatus, homeEndpoint, null);
    }

    public HomeEndpointHandler(final Status messageStatus,
                               final HomeEndpoint homeEndpoint,
                               final HomeEndpointHandler successor) {
        this.messageStatus = messageStatus;
        this.homeEndpoint = homeEndpoint;
        this.successor = successor;
    }


    public final void appendSuccessorToLast(final HomeEndpointHandler newLastSuccessor) {
        if (successor == null) {
            this.successor = newLastSuccessor;
        } else {
            successor.appendSuccessorToLast(newLastSuccessor);
        }
    }

    public final void handleMessage(StatusAndSessionIDMessage message, User user) {
        if (this.messageStatus != message.getStatus()) {
            if (successor != null) {
                successor.handleMessage(message, user);
            }
        } else {
            messageLogic(message, user);
        }
    }

    protected abstract void messageLogic(StatusAndSessionIDMessage message, User user);
}
