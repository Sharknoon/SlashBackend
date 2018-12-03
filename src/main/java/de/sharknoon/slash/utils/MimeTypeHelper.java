package de.sharknoon.slash.utils;

import java.util.Map;
import java.util.Map.Entry;

public final class MimeTypeHelper {
    public static final Map<String, String> validMimeTypes = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/gif", "gif"
    );

    private MimeTypeHelper() {
    }

    public static boolean hasValidMimeType(final String str) {
        return validMimeTypes.keySet().stream().anyMatch(str::contains);
    }

    public static String getMimeType(String data) {
        return validMimeTypes.keySet().stream().filter(data::contains).findAny().orElse("");
    }

    public static String getExtensionForMimeType(final String mimeType) {
        return validMimeTypes.get(mimeType);
    }

    public static String getExtension(final String data) {
        return validMimeTypes
                .entrySet()
                .stream()
                .filter(e -> data.contains(e.getKey()))
                .map(Entry::getValue)
                .findAny()
                .orElse("");
    }

}
