package com.github.fileshare.exceptions;

@SuppressWarnings("serial")
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
