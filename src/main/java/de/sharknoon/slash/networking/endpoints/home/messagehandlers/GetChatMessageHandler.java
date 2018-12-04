package de.sharknoon.slash.networking.endpoints.home.messagehandlers;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.Chat;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.home.Status;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.ChatResponse;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.ErrorResponse;
import de.sharknoon.slash.networking.endpoints.home.messagehandlers.response.HomeResponse;
import de.sharknoon.slash.networking.endpoints.home.messages.GetChatMessage;
import de.sharknoon.slash.networking.endpoints.home.messages.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.sessions.LoginSessions;
import de.sharknoon.slash.properties.Properties;
import de.sharknoon.slash.serialisation.Serialisation;
import org.bson.types.ObjectId;

import javax.websocket.Session;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class GetChatMessageHandler extends HomeEndpointMessageHandler {
    public GetChatMessageHandler(HomeEndpoint homeEndpoint, HomeEndpointMessageHandler successor) {
        super(Status.GET_CHAT, homeEndpoint, successor);
    }

    @Override
    public void messageLogic(StatusAndSessionIDMessage message, User user) {
        GetChatMessage getChatMessage = Serialisation.getGSON().fromJson(homeEndpoint.getLastMessage(), GetChatMessage.class);
        if (!ObjectId.isValid(getChatMessage.getPartnerUserID())) {
            ErrorResponse error = new ErrorResponse();
            error.status = "WRONG_USER_ID";
            error.description = "The specified userID doesn't conform to the right syntax";
            homeEndpoint.send(error);
        } else {
            ObjectId partnerID = new ObjectId(getChatMessage.getPartnerUserID());
            Optional<Chat> chat = DB.getChatByPartnerID(user.id, partnerID);
            Optional<User> partner = DB.getUser(partnerID);
            if (chat.isPresent() && partner.isPresent()) {
                ChatResponse cm = new ChatResponse();
                cm.chat = chat.get();
                cm.chat.partnerUsername = partner.get().username;
                homeEndpoint.send(cm);
            } else {
                if (partner.isEmpty()) {
                    ErrorResponse error = new ErrorResponse();
                    error.status = "NO_USER_FOUND";
                    error.description = "No user with the specified id was found";
                    homeEndpoint.send(error);
                } else {
                    Chat newChat = new Chat();
                    newChat.creationDate = LocalDateTime.now().withNano(0);
                    newChat.messages = Set.of();
                    newChat.personA = user.id;
                    newChat.personB = partner.get().id;
                    newChat.partnerUsername = partner.get().username;
                    newChat.id = new ObjectId();
                    DB.addChat(newChat);
                    ChatResponse cm = new ChatResponse();
                    cm.chat = newChat;
                    homeEndpoint.send(cm);

                    Set<User> chatMembers = Set.of(user, partner.get());
                    LoginSessions.getSessionsWithUser(HomeEndpoint.class, chatMembers)
                            .forEach(e -> {
                                User u = e.getValue();
                                Session s = e.getKey();
                                HomeResponse home = new HomeResponse();
                                home.projects = DB.getProjectsForUser(u);
                                home.chats = DB.getNLastChatsForUser(u.id, Properties.getUserConfig().amountfavouritechats());
                                for (Chat c : home.chats) {
                                    if (Objects.equals(c.personA, u.id)) {//I am user a
                                        c.partnerUsername = DB.getUser(c.personB).map(usr -> usr.username).orElse("ERROR");
                                    } else {
                                        c.partnerUsername = DB.getUser(c.personA).map(usr -> usr.username).orElse("ERROR");
                                    }
                                }
                                Endpoint.sendTo(s, home);
                            });
                }
            }
        }
    }
}
