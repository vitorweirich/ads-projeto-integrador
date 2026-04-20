package com.github.fileshare.exceptions;

@SuppressWarnings("serial")
public class PendingApprovalException extends RuntimeException {
    public PendingApprovalException(String message) {
        super(message);
    }
}
