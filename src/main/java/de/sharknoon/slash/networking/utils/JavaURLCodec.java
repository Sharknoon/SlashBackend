package de.sharknoon.slash.networking.utils;

import org.bson.BsonInvalidOperationException;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.net.MalformedURLException;
import java.net.URL;

public class JavaURLCodec implements Codec<URL> {
    @Override
    public URL decode(BsonReader bsonReader, DecoderContext decoderContext) {
        final String urlAsString = bsonReader.readString();
        try {
            return new URL(urlAsString);
        } catch (MalformedURLException e) {
            //e.printStackTrace();
            throw new BsonInvalidOperationException(
                    String.format("Could not create URL form String: %s", urlAsString));
        }
    }

    @Override
    public void encode(BsonWriter bsonWriter, URL url, EncoderContext encoderContext) {
        bsonWriter.writeString(url.toString());
    }

    @Override
    public Class<URL> getEncoderClass() {
        return URL.class;
    }
}
