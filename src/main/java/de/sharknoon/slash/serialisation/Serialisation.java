package de.sharknoon.slash.serialisation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.sharknoon.slash.serialisation.gsonconverter.LocalDateTimeConverter;
import de.sharknoon.slash.serialisation.gsonconverter.ObjectIdConverter;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public class Serialisation {
    
    //Gson to convert JSON
    private static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeConverter())
            .registerTypeAdapter(ObjectId.class, new ObjectIdConverter())
            .excludeFieldsWithModifiers()
            .create();
    
    public static Gson getGSON() {
        return GSON;
    }
}
