package com.neterium.client.sdk.model;

/**
 * Base class for "code-label" enumerations
 *
 * @author Bernard Ligny
 */
public interface BaseEnum {

    /**
     * The code value (when persisted)
     *
     * @return a code string
     */
    String getInternalCode();

}
