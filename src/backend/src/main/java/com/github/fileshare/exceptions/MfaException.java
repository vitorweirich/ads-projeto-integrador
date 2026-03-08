package com.github.fileshare.exceptions;

@SuppressWarnings("serial")
public class MfaException extends RuntimeException {
    public MfaException(String message) {
        super(message);
    }
}
