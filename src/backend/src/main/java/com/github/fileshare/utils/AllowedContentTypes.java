package com.github.fileshare.utils;

import java.util.Set;
import java.util.HashSet;

public class AllowedContentTypes {
    private static final Set<String> ALLOWED_TYPES = new HashSet<>();

    static {
        ALLOWED_TYPES.add("image/png");
        ALLOWED_TYPES.add("image/jpeg");
        ALLOWED_TYPES.add("video/mp4");
        ALLOWED_TYPES.add("video/avi");
        ALLOWED_TYPES.add("video/mkv");
        ALLOWED_TYPES.add("video/x-matroska");
        ALLOWED_TYPES.add("video/mov");
        ALLOWED_TYPES.add("video/quicktime");
        ALLOWED_TYPES.add("video/wmv");
        ALLOWED_TYPES.add("video/ts");
        ALLOWED_TYPES.add("application/pdf");
    }

    public static boolean isAllowed(String contentType) {
        return ALLOWED_TYPES.contains(contentType);
    }

    public static Set<String> getAllowedTypes() {
        return new HashSet<>(ALLOWED_TYPES);
    }
}
