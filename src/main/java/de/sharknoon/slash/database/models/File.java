package de.sharknoon.slash.database.models;

import com.google.gson.annotations.Expose;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

public class File {

    @BsonId
    @Expose
    public ObjectId id = new ObjectId();
    @Expose
    public String name = id.toHexString();
    @Expose
    public byte[] data = new byte[0];

}
