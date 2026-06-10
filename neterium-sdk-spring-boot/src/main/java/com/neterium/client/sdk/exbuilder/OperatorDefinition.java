package com.neterium.client.sdk.exbuilder;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * OperatorDefinition
 *
 * @author Bernard Ligny
 */
@Data
public class OperatorDefinition implements Displayable, Typed {

    @JsonView({Views.Summary.class, Views.Details.class})
    private final String id;

    @JsonView(Views.Details.class)
    private final String name;

    @JsonView(Views.Details.class)
    private final String type;


    public OperatorDefinition(Entry entry) {
        this.id = String.valueOf(entry.getId());
        this.type = entry.getType();
        this.name = entry.getTitle();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("name", name)
                .append("type", type)
                .build();
    }


    @Override
    @JsonView(Views.Summary.class)
    public String getLabel() {
        return this.name;
    }

}
