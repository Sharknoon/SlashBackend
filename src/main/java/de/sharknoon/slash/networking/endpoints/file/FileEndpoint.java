package de.sharknoon.slash.networking.endpoints.file;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.File;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import de.sharknoon.slash.networking.sessions.LoginSessions;
import de.sharknoon.slash.properties.Properties;
import de.sharknoon.slash.utils.MimeTypeHelper;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.Optional;

@ServerEndpoint("/file")
public class FileEndpoint extends Endpoint<FileMessage> {
    private static final String EMPTY_BASE64_ERROR_MSG
            = "{\"status\":\"NO_FILE\",\"message\":\"The file is null or empty!\"}";
    private static final String WRONG_BASE64_ERROR_MSG
            = "{\"status\":\"INCORRECT_FILE\",\"message\":\"The file is not of the correct type!\"}";
    private static final String FILE_UPLOADED_OK_MSG = "{\"status\":\"OK_FILE_UPLOAD\"}";
    private static final String FILE_ID_NOT_FOUND_MSG
            = "{\"status\":\"NO_FILE_FOR_ID\",\"message\":\"There is no file for the given ID!\"}";

    public FileEndpoint() {
        super(FileMessage.class);
    }

    @Override
    protected void onMessage(Session session, FileMessage message) {
        Optional<User> user = LoginSessions.getUser(message.getSessionid());

        if (user.isEmpty()) {//User not already logged in since the last server restart
            user = DB.getUserBySessionID(message.getSessionid());

            if (user.isEmpty()) {//User was never logged in
                send("{\"status\":\"NO_LOGIN_OR_TOO_MUCH_DEVICES\"," +
                        "\"messageType\":\"You are either not logged in or using more than " +
                        Properties.getUserConfig().maxdevices() + " devices\"}");
                return;
            }
        }
        LoginSessions.addSession(user.get(), message.getSessionid(), null, FileEndpoint.class, session);


        if (FileMessageStatus.SEND_FILE == message.getStatus()) {
            final String base64Data = message.getBase64Data();
            if (base64Data == null || base64Data.isEmpty() || base64Data.isBlank()) {
                session.getAsyncRemote().sendText(EMPTY_BASE64_ERROR_MSG);
            } else {
                if (MimeTypeHelper.hasValidMimeType(message.getBase64Data())) {
                    File newFIle = new File();
                    newFIle.data = base64Data.getBytes();
                    DB.addFile(newFIle);
                    session.getAsyncRemote().sendText(FILE_UPLOADED_OK_MSG);
                } else {
                    session.getAsyncRemote().sendText(WRONG_BASE64_ERROR_MSG);
                }
            }
        } else if (FileMessageStatus.GET_FILE == message.getStatus()) {
            Optional<File> file = DB.getFile(message.getDataId());
            if (file.isEmpty()) {
                session.getAsyncRemote().sendText(FILE_ID_NOT_FOUND_MSG);
            } else {
                final String fileString = new String(file.get().data);
                session.getAsyncRemote().sendText("{\"status\":\"OK_FILE\", \"data\":\"" + fileString + "\"}");
            }
        }
    }
}
