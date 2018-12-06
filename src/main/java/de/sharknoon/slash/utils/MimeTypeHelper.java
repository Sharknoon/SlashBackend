package de.sharknoon.slash.utils;

import java.util.*;
import java.util.Map.Entry;

public final class MimeTypeHelper {
    private static final Map<String, String> validMimeTypes = Map.of(
            "data:image/jpeg", "jpg",
            "data:image/png", "png",
            "data:image/gif", "gif"
    );
    
    public static Optional<String> getMimeType(final String data) {
        String d = data.trim();
        return validMimeTypes.keySet().stream().filter(d::startsWith).findAny();
    }
    
    
    public static boolean isValidMimeType(final String data) {
        String d = data.trim();
        return validMimeTypes.keySet().stream().anyMatch(d::startsWith);
    }
    
    public static String getExtensionForMimeType(final String mimeType) {
        return validMimeTypes.get(mimeType);
    }
    
    public static Optional<String> getExtension(final String data) {
        String d = data.trim();
        return validMimeTypes
                .entrySet()
                .stream()
                .filter(e -> d.startsWith(e.getKey()))
                .map(Entry::getValue)
                .findAny();
    }
    
}
