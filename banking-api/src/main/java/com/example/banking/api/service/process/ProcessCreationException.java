package com.example.banking.api.service.process;

/**
 * Exception thrown when a banking process cannot be created.
 */
public class ProcessCreationException extends Exception {
    
    public ProcessCreationException(String message) {
        super(message);
    }
    
    public ProcessCreationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ProcessCreationException(Throwable cause) {
        super(cause);
    }
}