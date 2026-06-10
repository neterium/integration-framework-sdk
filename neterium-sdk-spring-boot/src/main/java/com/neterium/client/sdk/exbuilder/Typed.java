package com.neterium.client.sdk.exbuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * Typed
 *
 * @author Bernard Ligny
 */
public interface Typed {

    @JsonView(Views.Details.class)
    String getType();

    @JsonIgnore
    default boolean needQuotes() {
        return "string".equalsIgnoreCase(getType());
    }

}
