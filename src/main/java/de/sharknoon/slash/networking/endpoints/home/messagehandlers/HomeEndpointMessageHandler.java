package de.sharknoon.slash.networking.endpoints.home.messagehandlers;

import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.Status;
import de.sharknoon.slash.networking.endpoints.home.messages.StatusAndSessionIDMessage;

public abstract class HomeEndpointMessageHandler {

    private final Status messageStatus;
    protected final HomeEndpoint homeEndpoint;
    protected HomeEndpointMessageHandler successor;


    public HomeEndpointMessageHandler(final Status messageStatus, final HomeEndpoint homeEndpoint) {
        this(messageStatus, homeEndpoint, null);
    }
    public HomeEndpointMessageHandler(final Status messageStatus,
                                      final HomeEndpoint homeEndpoint,
                                      final HomeEndpointMessageHandler successor) {
        this.messageStatus = messageStatus;
        this.homeEndpoint = homeEndpoint;
        this.successor = successor;
    }


    public final void appendSuccessorToLast(final HomeEndpointMessageHandler newLastSuccessor) {
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
