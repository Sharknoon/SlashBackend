package de.sharknoon.slash.networking.endpoints.file;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.File;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;
import de.sharknoon.slash.networking.sessions.LoginSessionUtils;

import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ServerEndpoint(
        value = "/file/{name}",
        decoders = FileEndpointDecoder.class
)
public class FileEndpoint extends Endpoint {
    private static final String UPLOAD_NOT_ALLOWED_ERROR_MSG
            = "{\"status\":\"UPLOAD_NOT_ALLOWED\",\"message\":\"The file upload has not been granted, use e.g. the send image function in the chat!\"}";
    private static final String FILE_UPLOADED_OK_MSG = "{\"status\":\"OK_FILE_UPLOAD\"}";
    private static final String FILE_UPLOADED_ERROR_MSG = "{\"status\":\"ERROR_FILE_UPLOAD\",\"message\":\"An unexpected error occurred during the upload\"}";
    private static final String FILE_NOT_FOUND_MSG
            = "{\"status\":\"FILE_NOT_FOUND\",\"message\":\"There is no file for the given name!\"}";

    private static final Set<String> READY_TO_UPLOAD_FILES = new HashSet<>();

    public static void allowUpload(String filename) {
        READY_TO_UPLOAD_FILES.add(filename);
    }

    @OnMessage
    public void onTextMessage(Session session, StatusAndSessionIDMessage message, @PathParam("name") String name) {
        Optional<User> optionalUser = LoginSessionUtils.checkLogin(message.getSessionid(), session, this);
        if (optionalUser.isEmpty()) {
            return;
        }

        Optional<File> file = DB.getFile(name);
        if (file.isEmpty()) {
            send(FILE_NOT_FOUND_MSG);
        } else {
            send(file.get().data);
        }

    }


    @OnMessage
    public void onBinaryMessage(Session session, byte[] binary, @PathParam("name") String name) {
        handleMessage(() -> {
            if (name == null || !READY_TO_UPLOAD_FILES.contains(name)) {
                send(UPLOAD_NOT_ALLOWED_ERROR_MSG);
                return;
            }
            //Only one upload per file is allowed
            READY_TO_UPLOAD_FILES.remove(name);
            File uploadedFile = new File();
            uploadedFile.data = binary;
            uploadedFile.name = name;
            boolean success = DB.addFile(uploadedFile);
            if (success) {
                send(FILE_UPLOADED_OK_MSG);
            } else {
                send(FILE_UPLOADED_ERROR_MSG);
            }
        });
    }
}
