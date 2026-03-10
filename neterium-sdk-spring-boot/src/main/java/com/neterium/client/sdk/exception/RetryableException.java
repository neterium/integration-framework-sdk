package com.neterium.client.sdk.exception;

/**
 * RetryableException
 *
 * @author Bernard Ligny
 */
public class RetryableException extends SdkException {

    /**
     * Constructs a new retryable exception with specified message and cause
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public RetryableException(String message, Throwable cause) {
        super(message, cause);
    }

}
