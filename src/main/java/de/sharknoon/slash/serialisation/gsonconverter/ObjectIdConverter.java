package de.sharknoon.slash.serialisation.gsonconverter;

import com.google.gson.*;
import org.bson.types.ObjectId;

import java.lang.reflect.Type;

public class ObjectIdConverter implements JsonSerializer<ObjectId>, JsonDeserializer<ObjectId> {
    @Override
    public ObjectId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new ObjectId(json.getAsString());
    }
    
    @Override
    public JsonElement serialize(ObjectId src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toHexString());
    }
}
