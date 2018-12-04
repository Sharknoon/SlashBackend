package de.sharknoon.slash.networking.endpoints.home.messagehandlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.Chat;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.database.models.message.Message;
import de.sharknoon.slash.networking.LoginSessions;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.Status;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.ChatResponse;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.ErrorResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.AddChatMessageMessage;
import de.sharknoon.slash.networking.endpoints.home.messages.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.pushy.PushStatus;
import de.sharknoon.slash.networking.pushy.Pushy;
import de.sharknoon.slash.serialisation.Serialisation;
import org.bson.types.ObjectId;

import java.util.Objects;
import java.util.Optional;

public class AddChatMessageMessageHandler extends HomeEndpointMessageHandler {
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
                Message message1 = optionalMessage.get();
                DB.addMessageToChat(c, message1);
                ChatResponse cr = new ChatResponse();
                cr.chat = c;
                c.partnerUsername = partner.get().username;
                LoginSessions.getSession(HomeEndpoint.class, user).ifPresent(session -> homeEndpoint.send(cr));
                Pushy.sendPush(PushStatus.NEW_CHAT_MESSAGE, message1, user.username, partner.get());
                c.partnerUsername = user.username;
                LoginSessions.getSession(HomeEndpoint.class, partner.get()).ifPresent(session -> homeEndpoint.send(cr));
            }
        }
    }
}
