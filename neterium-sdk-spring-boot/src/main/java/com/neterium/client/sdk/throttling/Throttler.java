package com.neterium.client.sdk.throttling;

import java.time.Duration;

/**
 * Ability to calibrate a "value" (typically a bandwidth, a pool size)
 * depending on statistics collected by recording response times and errors
 *
 * @author Bernard Ligny
 */
public interface Throttler {

    /**
     * Register a duration of a successful operation
     *
     * @param duration the duration to register
     */
    void registerDuration(Duration duration);

    /**
     * Register an operation timeout
     *
     * @param duration the timeout duration to register
     */
    void registerTimeOut(Duration duration);

    /**
     * Register a negative result of type "Quota Exceeded"
     */
    void registerQuotaExceeded();

    /**
     * Register a new {@link ThrottlerListener}
     *
     * @param listener the listener to register
     */
    void addListener(ThrottlerListener listener);

}
