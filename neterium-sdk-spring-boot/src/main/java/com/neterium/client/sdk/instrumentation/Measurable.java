package com.neterium.client.sdk.instrumentation;

import java.util.Map;

/**
 * Ability of being measured (typically for monitoring purposes)
 *
 * @author Bernard Ligny
 */
public interface Measurable {

    /**
     * Describe the measured object in terms of instrumentation-based properties
     *
     * @return a map of (key, value) pairs
     */
    Map<String, Object> describe();

    /**
     * Alias denoting the "owner" object, allowing some categorization by the consumer
     *
     * @return the category name
     */
    String getCategoryName();

}
