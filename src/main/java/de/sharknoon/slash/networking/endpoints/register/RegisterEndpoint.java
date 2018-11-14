package de.sharknoon.slash.networking.endpoints.register;

import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.time.LocalDateTime;
import java.util.Set;

@ServerEndpoint("/register")
public class RegisterEndpoint extends Endpoint<RegisterMessage> {
    
    //Needs to stay public
    @SuppressWarnings("WeakerAccess")
    public RegisterEndpoint() {
        super(RegisterMessage.class);
    }
    
    /**
     * Registers a new user
     *
     * @param message The user registration data
     * @return true if the registration was successful
     */
    private static boolean register(RegisterMessage message) {
        String salt = BCrypt.gensalt();
        hashPasswordOnRegister(message, salt);
        LocalDateTime registrationDate = LocalDateTime.now();
        
        User user = new User();
        user.username = message.getUsername();
        user.email = message.getEmail();
        user.password = message.getPassword();
        user.salt = salt;
        user.registrationDate = registrationDate;
        user.sessionIDs = Set.of();
        
        return DB.register(user);
    }
    
    private static void hashPasswordOnRegister(RegisterMessage message, String salt) {
        String saltedPassword = BCrypt.hashpw(message.getPassword(), salt);
        message.setPassword(saltedPassword);
    }
    
    private boolean checkEmailSyntax(String email) {
        return email.matches("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])");
    }
    
    private boolean checkUsernameSyntax(String username) {
        return username.length() >= 1 && username.length() <= 20;
    }
    
    @Override
    protected void onMessage(Session session, RegisterMessage message) {
        String returnMessage;
    
        if (DB.existsUsername(message.getUsername())) {
            returnMessage = "{\"status\":\"USERNAME_ALREADY_REGISTERED\",\"message\":\"The specified username is already taken by another account. Please choose another one.\"}";
        } else if (DB.existsEmail(message.getEmail())) {
            returnMessage = "{\"status\":\"EMAIL_ALREADY_REGISTERED\",\"message\":\"The specified email is already taken by another account. Please log in.\"}";
        } else if (!checkUsernameSyntax(message.getUsername())) {
            returnMessage = "{\"status\":\"WRONG_USERNAME\",\"message\":\"The specified username does not meet the specifications\"}";
        } else if (!checkEmailSyntax(message.getEmail())) {
            returnMessage = "{\"status\":\"WRONG_EMAIL\",\"message\":\"The specified email does not meet the specifications\"}";
        } else if (!register(message)) {
            returnMessage = "{\"status\":\"ERROR\",\"message\":\"Could not register the user due to an unexpected error\"}";
        } else {
            returnMessage = "{\"status\":\"OK\",\"message\":\"Successfully registered\"}";
        }
        
        session.getAsyncRemote().sendText(returnMessage);
    }
    
}
