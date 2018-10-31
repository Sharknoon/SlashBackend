package de.sharknoon.slash.database.models;

import de.sharknoon.slash.database.Values;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.logging.*;

public class User {
    
    private final Document userDocument;
    private String username;
    private String email;
    private String password;
    private String salt;
    private LocalDateTime registrationDate;
    
    public User(Document userDocument) {
        this.userDocument = userDocument;
    }
    
    
    public String getUsername() {
        if (username == null) {
            username = userDocument.getString(Values.USERS_COLLECTION_USERNAME);
            if (username == null) {
                Logger.getGlobal().log(Level.WARNING, "Username not in User-Document from DB!");
                username = "";
            }
        }
        return username;
    }
    
    public String getEmail() {
        if (email == null) {
            email = userDocument.getString(Values.USERS_COLLECTION_EMAIL);
            if (email == null) {
                Logger.getGlobal().log(Level.WARNING, "Email not in User-Document from DB!");
                email = "";
            }
        }
        return email;
    }
    
    public String getPassword() {
        if (password == null) {
            password = userDocument.getString(Values.USERS_COLLECTION_PASSWORD);
            if (password == null) {
                Logger.getGlobal().log(Level.WARNING, "Password not in User-Document from DB!");
                password = "";
            }
        }
        return password;
    }
    
    public String getSalt() {
        if (salt == null) {
            salt = userDocument.getString(Values.USERS_COLLECTION_SALT);
            if (salt == null) {
                Logger.getGlobal().log(Level.WARNING, "Salt not in User-Document from DB!");
                salt = "";
            }
        }
        return salt;
    }
    
    public LocalDateTime getRegistrationDate() {
        if (registrationDate == null) {
            String registrationDateString = userDocument.getString(Values.USERS_COLLECTION_REGISTRATION_DATE);
            if (registrationDateString == null) {
                Logger.getGlobal().log(Level.WARNING, "Registration Date not in User-Document from DB!");
                registrationDate = LocalDateTime.MIN;
            } else {
                try {
                    registrationDate = LocalDateTime.parse(registrationDateString);
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, "Registration Date from the DB is not parsable! " + registrationDateString);
                    registrationDate = LocalDateTime.MIN;
                }
            }
        }
        return registrationDate;
    }
    
}
