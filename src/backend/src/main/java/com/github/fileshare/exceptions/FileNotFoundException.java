package com.github.fileshare.exceptions;

@SuppressWarnings("serial")
public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(String message) {
        super(message);
    }
}
