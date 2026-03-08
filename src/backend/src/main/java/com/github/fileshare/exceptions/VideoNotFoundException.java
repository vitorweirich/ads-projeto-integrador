package com.github.fileshare.exceptions;

@SuppressWarnings("serial")
public class VideoNotFoundException extends RuntimeException {
    public VideoNotFoundException(String message) {
        super(message);
    }
}
