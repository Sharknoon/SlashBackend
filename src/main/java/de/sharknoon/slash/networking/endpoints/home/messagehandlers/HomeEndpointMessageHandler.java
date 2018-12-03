package de.sharknoon.slash.networking.endpoints.home.messagehandlers;

import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.messages.StatusAndSessionIDMessage;

public abstract class HomeEndpointMessageHandler {

    protected final HomeEndpoint homeEndpoint;
    protected HomeEndpointMessageHandler successor;

    public HomeEndpointMessageHandler(final HomeEndpoint homeEndpoint, final HomeEndpointMessageHandler successor) {
        this.homeEndpoint = homeEndpoint;
        this.successor = successor;
    }

    public void appendSuccessorToLast(final HomeEndpointMessageHandler newLastSuccessor) {
        if (successor == null) {
            this.successor = newLastSuccessor;
        } else {
            successor.appendSuccessorToLast(newLastSuccessor);
        }
    }

    public abstract void handleMessage(StatusAndSessionIDMessage message, User user);
}
