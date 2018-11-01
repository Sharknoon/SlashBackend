package de.sharknoon.slash.database;

public enum Values {
    
    //Collections
    USERS_COLLECTION("users"),
    PROJECTS_COLLECTION("projects"),
    CHATS_COLLECTION("chats"),
    
    //All collections
    COLLECTION_ID("_id"),
    
    //Collection users
    USERS_COLLECTION_USERNAME("username"),
    USERS_COLLECTION_EMAIL("email"),
    USERS_COLLECTION_PASSWORD("password"),
    USERS_COLLECTION_SALT("salt"),
    USERS_COLLECTION_REGISTRATION_DATE("registrationDate"),
    USERS_COLLECTION_SESSION_IDS("sessionIds"),
    
    //Collection projects
    PROJECTS_COLLECTION_NAME("name"),
    PROJECTS_COLLECTION_IMAGE("image"),
    PROJECTS_COLLECTION_CREATION_DATE("creationDate"),
    PROJECTS_COLLECTION_USERS("users"),
    
    //Collections chats
    CHATS_COLLECTION_PERSON_A("personA"),
    CHATS_COLLECTION_PERSON_B("personB"),
    CHATS_COLLECTION_CREATION_DATE("creationDate"),
    ;
    
    public final String value;
    
    Values(String value) {
        this.value = value;
    }
}
