package de.sharknoon.slash.database;

public enum Values {
    
    //Collections
    USERS_COLLECTION("users"),
    PROJECTS_COLLECTION("projects"),
    CHATS_COLLECTION("chats"),
    FILES_BUCKET("files"),
    
    //All collections
    COLLECTION_ID("_id"),
    
    //Collection users
    USERS_COLLECTION_USERNAME("username"),
    USERS_COLLECTION_EMAIL("email"),
    // --Commented out by Inspection START (14.11.2018 13:35):
//// --Commented out by Inspection START (14.11.2018 13:35):
////    // --Commented out by Inspection (14.11.2018 13:35):USERS_COLLECTION_PASSWORD("password"),
// --Commented out by Inspection STOP (14.11.2018 13:35)
//    USERS_COLLECTION_SALT("salt"),
//    USERS_COLLECTION_REGISTRATION_DATE("registrationDate"),
// --Commented out by Inspection STOP (14.11.2018 13:35)
    USERS_COLLECTION_SESSION_IDS("sessionIDs"),
    USERS_COLLECTION_DEVICE_IDS("deviceIDs"),
    
    // --Commented out by Inspection START (14.11.2018 13:35):
//    //Collection projects
//    PROJECTS_COLLECTION_NAME("name"),
// --Commented out by Inspection STOP (14.11.2018 13:35)
    // --Commented out by Inspection (14.11.2018 13:35):PROJECTS_COLLECTION_IMAGE("image"),
    // --Commented out by Inspection (14.11.2018 13:35):PROJECTS_COLLECTION_CREATION_DATE("creationDate"),
    PROJECTS_COLLECTION_USERS("users"),
    
    //Collections chats
    CHATS_COLLECTION_PERSON_A("personA"),
    CHATS_COLLECTION_PERSON_B("personB"),
    CHATS_COLLECTION_CREATION_DATE("creationDate"),
    CHATS_COLLECTION_MESSAGES("messages"),
    CHATS_COLLECTION_MESSAGES_CREATION_DATE("creationDate"),
    PROJECTS_COLLECTION_MESSAGES("messages");
    
    public final String value;
    
    Values(String value) {
        this.value = value;
    }
}
