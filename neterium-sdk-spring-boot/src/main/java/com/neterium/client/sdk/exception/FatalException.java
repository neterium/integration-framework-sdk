package com.neterium.client.sdk.exception;

/**
 * FatalException
 *
 * @author Bernard Ligny
 */
public class FatalException extends SdkException {

    /**
     * Constructs a new fatal exception with specified cause
     *
     * @param cause the cause
     */
    public FatalException(Throwable cause) {
        super(cause);
    }

}
