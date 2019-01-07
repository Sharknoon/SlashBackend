package de.sharknoon.slash.networking.endpoints.login;

import de.sharknoon.slash.serialisation.Serialisation;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class LoginEndpointDecoder implements Decoder.Text<LoginMessage> {


    @Override
    public LoginMessage decode(String s) throws DecodeException {
        try {
            return Serialisation.getGSON().fromJson(s, LoginMessage.class);
        } catch (Exception e) {
            throw new DecodeException(s, e.getLocalizedMessage(), e);
        }
    }

    @Override
    public boolean willDecode(String s) {
        return s != null;
    }

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }
}
