package de.sharknoon.slash.networking.sessions;

import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.Endpoint;
import de.sharknoon.slash.networking.endpoints.file.FileEndpoint;
import de.sharknoon.slash.networking.endpoints.home.HomeEndpoint;
import de.sharknoon.slash.networking.endpoints.login.LoginEndpoint;
import de.sharknoon.slash.networking.endpoints.register.RegisterEndpoint;

import javax.websocket.Session;

class LoginSession {
    private final User user;
    private Session loginSession = null;
    private Session registerSession = null;
    private Session homeSession = null;
    private Session fileSession = null;
    
    LoginSession(User user) {
        this.user = user;
    }

    Session getSession(Class<? extends Endpoint> endpoint) {
        if (endpoint == HomeEndpoint.class) {
            return homeSession;
        } else if (endpoint == LoginEndpoint.class) {
            return loginSession;
        } else if (endpoint == RegisterEndpoint.class) {
            return registerSession;
        } else if (endpoint == FileEndpoint.class) {
            return fileSession;
        }
        return null;
    }


    void setSession(Class<? extends Endpoint> endpoint, Session session) {
        if (endpoint == HomeEndpoint.class) {
            homeSession = session;
        } else if (endpoint == LoginEndpoint.class) {
            loginSession = session;
        } else if (endpoint == RegisterEndpoint.class) {
            registerSession = session;
        } else if (endpoint == FileEndpoint.class) {
            fileSession = session;
        }
    }

    User getUser() {
        return user;
    }

    Session getLoginSession() {
        return loginSession;
    }

    Session getRegisterSession() {
        return registerSession;
    }

    Session getHomeSession() {
        return homeSession;
    }

    Session getFileSession() {
        return fileSession;
    }
}