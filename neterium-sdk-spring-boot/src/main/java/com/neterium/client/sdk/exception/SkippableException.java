package com.neterium.client.sdk.exception;

/**
 * SkippableException
 *
 * @author Bernard Ligny
 */
public class SkippableException extends SdkException {

    /**
     * Constructs a new skippable exception with specified message and cause
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public SkippableException(String message, Throwable cause) {
        super(message, cause);
    }

}
