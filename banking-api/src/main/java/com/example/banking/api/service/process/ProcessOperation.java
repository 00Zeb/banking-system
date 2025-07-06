package com.example.banking.api.service.process;

/**
 * Interface for operations that can be executed against the banking application process.
 * This interface follows the Command pattern to encapsulate process operations.
 *
 * @param <T> the type of result returned by the operation
 */
@FunctionalInterface
public interface ProcessOperation<T> {
    
    /**
     * Executes the operation using the provided process communication.
     *
     * @param communication the process communication interface
     * @return the result of the operation
     * @throws Exception if the operation fails
     */
    T execute(ProcessCommunication communication) throws Exception;
}