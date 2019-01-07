package de.sharknoon.slash.networking.endpoints.register;

import de.sharknoon.slash.serialisation.Serialisation;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class RegisterEndpointDecoder implements Decoder.Text<RegisterMessage> {
    @Override
    public RegisterMessage decode(String s) throws DecodeException {
        try {
            return Serialisation.getGSON().fromJson(s, RegisterMessage.class);
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
