package com.neterium.client.sdk.throttling;

/**
 * Interface to be implemented by components that need to listen to
 * {@link Throttler} events or changes
 *
 * @author Bernard Ligny
 */
@FunctionalInterface
public interface ThrottlerListener {

    /**
     * Get notified about an update of the throttler value
     *
     * @param throttlerId id of updated throttler
     * @param oldValue    old throttler value
     * @param newValue    new throttler value
     */
    void onValueUpdated(String throttlerId, int oldValue, int newValue);

}
