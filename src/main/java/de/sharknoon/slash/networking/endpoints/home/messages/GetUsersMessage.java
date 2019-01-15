package de.sharknoon.slash.networking.endpoints.home.messages;

import com.google.gson.annotations.Expose;
import de.sharknoon.slash.networking.endpoints.Status;
import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;

import java.util.Objects;

public class GetUsersMessage extends StatusAndSessionIDMessage {

    @Expose
    private String search = "";

    public GetUsersMessage() {
        super(Status.GET_USERS);
    }

    public String getSearch() {
        return Objects.requireNonNullElse(search, "");
    }

    public void setSearch(String search) {
        if (search != null) {
            this.search = search;
        }
    }
}
