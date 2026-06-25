package com.neterium.client.sdk.exbuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * Typed
 *
 * @author Bernard Ligny
 */
public interface Typed {

    /**
     * Get type
     *
     * @return type name
     */
    @JsonView(Views.Details.class)
    String getType();

    /**
     * Whether this type requires values to be quoted or not
     *
     * @return type name
     */
    @JsonIgnore
    default boolean needQuotes() {
        return "string".equalsIgnoreCase(getType());
    }

}
