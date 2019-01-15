package de.sharknoon.slash.networking.endpoints.file;

import de.sharknoon.slash.networking.endpoints.StatusAndSessionIDMessage;
import de.sharknoon.slash.serialisation.Serialisation;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class FileEndpointDecoder implements Decoder.Text<StatusAndSessionIDMessage> {
    @Override
    public StatusAndSessionIDMessage decode(String s) throws DecodeException {
        try {
            return Serialisation.getGSON().fromJson(s, StatusAndSessionIDMessage.class);
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
