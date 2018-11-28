package de.sharknoon.slash.networking.utils;

import java.io.*;
import java.net.URLConnection;
import java.util.Map;

public final class MimeTypeHelper {
    public static final Map<String, String> validMimeTypes = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png,",
            "image/gif", "gif"
    );
    
    private MimeTypeHelper() {
    }
    
    public static boolean hasValidMimeType(final byte[] data) throws IOException {
        return validMimeTypes.containsKey(getMimeType(data));
    }
    
    public static String getMimeType(byte[] data) throws IOException {
        InputStream is = new ByteArrayInputStream(data);
        return URLConnection.guessContentTypeFromStream(is);
    }
    
    public static String getExtensionForMimeType(final String mimeType) {
        return validMimeTypes.get(mimeType);
    }
    
}
