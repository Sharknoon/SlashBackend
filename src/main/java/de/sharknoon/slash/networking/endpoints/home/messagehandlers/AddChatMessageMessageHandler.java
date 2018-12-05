package de.sharknoon.slash.networking.endpoints.home.messagehandlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.*;
import de.sharknoon.slash.database.models.message.Message;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import de.sharknoon.slash.networking.endpoints.home.*;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.*;
import de.sharknoon.slash.networking.endpoints.home.messages.*;
import de.sharknoon.slash.networking.pushy.*;
import de.sharknoon.slash.networking.sessions.LoginSessions;
import de.sharknoon.slash.serialisation.Serialisation;
import org.bson.types.ObjectId;

import java.util.*;

public class AddChatMessageMessageHandler extends HomeEndpointMessageHandler {
    
    public AddChatMessageMessageHandler(HomeEndpoint homeEndpoint) {
        super(Status.ADD_CHAT_MESSAGE, homeEndpoint);
    }
    
    public AddChatMessageMessageHandler(HomeEndpoint homeEndpoint, HomeEndpointMessageHandler successor) {
        super(Status.ADD_CHAT_MESSAGE, homeEndpoint, successor);
    }
    
    @Override
    public void messageLogic(StatusAndSessionIDMessage message, User user) {
        AddChatMessageMessage addChatMessageMessage = Serialisation.getGSON().fromJson(homeEndpoint.getLastMessage(), AddChatMessageMessage.class);
        Optional<Chat> chat;
        if (!ObjectId.isValid(addChatMessageMessage.getChatID())) {
            //wrong chat id syntax
            ErrorResponse error = new ErrorResponse();
            error.status = "WRONG_CHAT_ID";
            error.description = "The syntax of the chat-ID was not correct";
            homeEndpoint.send(error);
        } else if ((chat = DB.getChat(new ObjectId(addChatMessageMessage.getChatID()))).isEmpty()) {
            //chat id doesn't exist
            ErrorResponse error = new ErrorResponse();
            error.status = "NO_CHAT_FOUND";
            error.description = "No corresponding chat to the chat-ID was found";
            homeEndpoint.send(error);
        } else {
            Chat c = chat.get();
            Optional<User> partner;
            if (Objects.equals(c.personA, user.id)) {//I am user a
                partner = DB.getUser(c.personB);
            } else {
                partner = DB.getUser(c.personA);
            }
            if (partner.isEmpty()) {
                //partner doesn't exists
                ErrorResponse error = new ErrorResponse();
                error.status = "CHAT_PARTNER_NOT_FOUND";
                error.description = "No corresponding chat partner to the chat-ID was found";
                homeEndpoint.send(error);
            } else {
                Optional<Message> optionalMessage = homeEndpoint.fillMessage(addChatMessageMessage, user, true);
                //errors already send
                if (optionalMessage.isEmpty()) {
                    return;
                }
                Message m = optionalMessage.get();
                DB.addMessageToChat(c, m);
                ChatResponse cr = new ChatResponse();
                cr.chat = c;
                c.partnerUsername = partner.get().username;
                homeEndpoint.send(cr);
                Pushy.sendPush(PushStatus.NEW_CHAT_MESSAGE, c.id.toHexString(), m, user.username, partner.get());
                c.partnerUsername = user.username;
                LoginSessions
                        .getSession(HomeEndpoint.class, partner.get())
                        .ifPresent(session -> Endpoint.sendTo(session, cr));
            }
        }
    }
}
