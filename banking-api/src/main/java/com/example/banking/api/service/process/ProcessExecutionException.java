package com.example.banking.api.service.process;

/**
 * Exception thrown when process execution fails.
 */
public class ProcessExecutionException extends RuntimeException {
    
    public ProcessExecutionException(String message) {
        super(message);
    }
    
    public ProcessExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}