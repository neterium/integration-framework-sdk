package com.neterium.client.sdk.exception;

/**
 * Base class for run-time exceptions raised by the JDK
 *
 * @author Bernard Ligny
 */
public class SdkException extends RuntimeException {

    /**
     * Constructs a new SDK exception with specified message and cause
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public SdkException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new SDK exception with specified message
     *
     * @param message the detail message
     */
    public SdkException(String message) {
        super(message);
    }

    /**
     * Constructs a new SDK exception with specified cause
     *
     * @param cause the cause
     */
    public SdkException(Throwable cause) {
        super(cause);
    }

}
