package com.neterium.client.sdk.exbuilder;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * FunctionDefinition
 *
 * @author Bernard Ligny
 */
@Data
public class FunctionDefinition implements Displayable, Typed {

    @JsonView({Views.Summary.class, Views.Details.class})
    private final String id;

    @JsonView(Views.Details.class)
    private final String name;

    @JsonView(Views.Summary.class)
    private final String label;

    @JsonView(Views.Details.class)
    private final String returnType;

    @JsonView(Views.Details.class)
    private final List<FunctionArg> arguments = new ArrayList<>();

    @JsonView(Views.Details.class)
    private String description;


    public FunctionDefinition(Entry entry, String syntax, int idx) {
        this.id = String.valueOf(entry.getId()) + "." + idx;
        this.label = syntax;
        this.returnType = entry.getType();
        this.name = StringUtils.substringBefore(syntax, '(');
        this.description = entry.getDescription();
        this.grabParameters(syntax);
    }


    private void grabParameters(String syntax) {
        var allArgsString = StringUtils.substringBetween(syntax, "(", ")");
        var allArgs = StringUtils.split(allArgsString, ",");
        for (int i = 0; i < allArgs.length; i++) {
            var tokens = StringUtils.split(allArgs[i], ':');
            var fctArg = new FunctionArg(i, tokens[0].trim(), tokens[1].trim());
            this.arguments.add(fctArg);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("name", name)
                .append("arguments", arguments)
                .build();
    }


    @Override
    public String getType() {
        return returnType;
    }

}
