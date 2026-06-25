package com.neterium.client.sdk.exbuilder;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * Displayable
 *
 * @author Bernard Ligny
 */
public interface Displayable {

    /**
     * Get label
     *
     * @return label
     */
    @JsonView(Views.Summary.class)
    String getLabel();

}
