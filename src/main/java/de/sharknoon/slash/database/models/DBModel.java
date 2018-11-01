package de.sharknoon.slash.database.models;

import de.sharknoon.slash.database.Values;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.logging.*;

public abstract class DBModel {
    
    private final Document modelDocument;
    private String id;
    
    DBModel(Document modelDocument) {
        this.modelDocument = modelDocument;
    }
    
    public String getId() {
        if (id == null) {
            ObjectId objectId = modelDocument.getObjectId("_id");
            if (objectId == null) {
                Logger.getGlobal().log(Level.WARNING, "ID not in User-Document from DB!");
                id = "";
            } else {
                id = objectId.toString();
            }
        }
        return id;
    }
    
    String getString(Values key) {
        if (modelDocument.containsKey(key.value)) {
            Object o = modelDocument.get(key.value);
            return String.valueOf(o);
        }
        return null;
    }
    
    List<?> getList(Values key) {
        if (modelDocument.containsKey(key.value)) {
            Object o = modelDocument.get(key.value);
            if (o instanceof List) {
                return (List<?>) o;
            }
        }
        return null;
    }
}
