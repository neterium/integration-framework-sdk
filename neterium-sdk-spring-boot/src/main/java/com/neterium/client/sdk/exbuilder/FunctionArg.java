package com.neterium.client.sdk.exbuilder;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;

/**
 * FunctionArg
 *
 * @author Bernard Ligny
 */
@Data
public class FunctionArg implements Displayable, Typed {

    @JsonView({Views.Summary.class, Views.Details.class})
    private final int pos;

    @JsonView(Views.Details.class)
    private final String name;

    @JsonView(Views.Details.class)
    private final String type;

    @Override
    public String getLabel() {
        return this.name;
    }

}
