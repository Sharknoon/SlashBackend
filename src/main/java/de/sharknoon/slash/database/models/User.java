package de.sharknoon.slash.database.models;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.database.models.sentiment.Sentiment;
import org.apache.commons.lang3.StringUtils;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

public class User {

    @BsonId
    @Expose
    public ObjectId id = new ObjectId();
    @Expose
    public String username = StringUtils.EMPTY;
    @Expose
    public Sentiment sentiment = new Sentiment();
    public String email = StringUtils.EMPTY;
    public String password = StringUtils.EMPTY;
    public String salt = StringUtils.EMPTY;
    public LocalDateTime registrationDate = LocalDateTime.now().withNano(0);
    public Set<Login> ids = Set.of();

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return Objects.equals(id, user.id);
    }


}
