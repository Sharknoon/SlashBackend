package de.sharknoon.slash.serialisation;

import com.google.gson.*;
import de.sharknoon.slash.networking.utils.*;
import org.bson.types.ObjectId;

import java.lang.reflect.Modifier;
import java.time.LocalDateTime;

public class Serialisation {
    
    //Gson to convert JSON
    private static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeConverter())
            .registerTypeAdapter(ObjectId.class, new ObjectIdConverter())
            .excludeFieldsWithModifiers(Modifier.STATIC)
            .create();
    
    public static Gson getGSON() {
        return GSON;
    }
}
