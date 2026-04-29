package com.neterium.client.sdk.exbuilder;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ExpressionBuilder
 *
 * @author Bernard Ligny
 */
@Component
public class ExpressionBuilder {

    private final ModelLoader loader;

    public ExpressionBuilder(ModelLoader loader) {
        this.loader = loader;
    }


    public String dataCondition(String pathId, String operatorId, String value) {
        var path = loader.getDataPath(pathId);
        var operator = loader.getOperator(operatorId);
        return String.join(" ", path.getName(), operator.getName(), format(value, operator));
    }


    public String matcherCondition(String functionId,
                                   Map<String, String> bindings,
                                   String operatorId,
                                   String value) {
        var fct = loader.getFunction(functionId);
        var fctInvocation = new StringBuilder()
                .append(fct.getName())
                .append("(");
        for (int i = 0; i < fct.getArguments().size(); i++) {
            var argDef = fct.getArguments().get(i);
            var httpValue = bindings.get(argDef.getName());
            var argValue = format(httpValue, argDef);
            if (i > 0) {
                fctInvocation.append(", ");
            }
            fctInvocation.append(argValue);
        }
        fctInvocation.append(")");
        var operator = loader.getOperator(operatorId);
        return String.join(" ", fctInvocation, operator.getName(), format(value, operator));
    }


    private String format(String value, Typed definition) {
        if (definition.needQuotes()) {
            return "'" + value + "'"; // TODO: escaping ?
        } else {
            return value;
        }
    }


}
