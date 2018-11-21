package de.sharknoon.slash.database.models;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang3.StringUtils;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Set;

public class User {
    
    @BsonId
    @Expose
    public ObjectId id;
    @Expose
    public String username = StringUtils.EMPTY;
    public String email = StringUtils.EMPTY;
    public String password = StringUtils.EMPTY;
    public String salt = StringUtils.EMPTY;
    public LocalDateTime registrationDate = LocalDateTime.now();
    public Set<String> sessionIDs = Set.of();
    public Set<String> deviceIDs = Set.of();
    
}
