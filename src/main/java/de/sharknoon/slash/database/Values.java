package de.sharknoon.slash.database;

public enum Values {

    //Collections
    COLLECTION_USERS("users"),
    COLLECTION_PROJECTS("projects"),
    COLLECTION_CHATS("chats"),
    COLLECTION_FILES("files"),

    //All collections
    COLLECTION_ID("_id"),

    //Collection users
    USERS_COLLECTION_USERNAME("username"),
    USERS_COLLECTION_EMAIL("email"),
    USERS_COLLECTION_IDS("ids"),
    USERS_COLLECTION_IDS_SESSION_ID("sessionID"),
    USERS_COLLECTION_IDS_DEVICE_ID("deviceID"),
    USERS_COLLECTION_SENTIMENT("sentiment"),
    USERS_COLLECTION_IMAGE("image"),

    //Collections chats
    CHATS_COLLECTION_PERSON_A("personA"),
    CHATS_COLLECTION_PERSON_B("personB"),
    CHATS_COLLECTION_MESSAGES("messages"),
    CHATS_COLLECTION_MESSAGES_CREATION_DATE("creationDate"),

    //Collection projects
    PROJECTS_COLLECTION_USERS("users"),
    PROJECTS_COLLECTION_PROJECT_OWNER("projectOwner"),
    PROJECTS_COLLECTION_MESSAGES("messages"),
    PROJECTS_COLLECTION_SENTIMENT("sentiment"),
    PROJECTS_COLLECTION_IMAGE("image");

    public final String value;

    Values(String value) {
        this.value = value;
    }
}
