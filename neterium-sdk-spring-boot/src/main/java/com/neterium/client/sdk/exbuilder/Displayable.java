package com.neterium.client.sdk.exbuilder;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * PathDefinition
 *
 * @author Bernard Ligny
 */
public interface Displayable {

    @JsonView(Views.Summary.class)
    String getLabel();

}
