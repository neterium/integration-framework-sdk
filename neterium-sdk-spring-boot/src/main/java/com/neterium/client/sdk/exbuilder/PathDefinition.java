package com.neterium.client.sdk.exbuilder;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * PathDefinition
 *
 * @author Bernard Ligny
 */
@Data
public class PathDefinition implements Displayable {

    @JsonView(Views.Summary.class)
    private final String id;

    @JsonView(Views.Details.class)
    private final String name;

    @JsonView({Views.Summary.class, Views.Details.class})
    private final String category;

    @JsonView(Views.Details.class)
    private final int scope; // 2-bits mask


    public PathDefinition(Entry entry, String syntax) {
        this.id = String.valueOf(entry.getId());
        this.name = syntax;
        this.category = entry.getTitle();
        this.scope = (entry.isJetFlow() ? 1 : 0) + (entry.isJetScan() ? 2 : 0);
    }


    @Override
    @JsonView(Views.Summary.class)
    public String getLabel() {
        return this.name;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("category", category)
                .append("name", name)
                .append("scope", scope)
                .build();
    }

}
