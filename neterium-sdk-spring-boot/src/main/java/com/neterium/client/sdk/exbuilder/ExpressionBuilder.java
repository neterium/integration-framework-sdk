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

    private final Dictionary dictionary;

    /**
     * Constructor
     *
     * @param dictionary dictionary of available functions, operators, etc
     */
    public ExpressionBuilder(Dictionary dictionary) {
        this.dictionary = dictionary;
    }


    /**
     * Create a DATA condition
     *
     * @param pathId     id of path definition
     * @param operatorId id of operator
     * @param value      value
     * @return an expression
     */
    public String dataCondition(String pathId, String operatorId, String value) {
        var path = dictionary.getDataPath(pathId);
        var operator = dictionary.getOperator(operatorId);
        return String.join(" ", path.getName(), operator.getName(), format(value, operator));
    }


    /**
     * Create a MATCHER condition
     *
     * @param functionId id of function
     * @param bindings   bindings for function parameters
     * @param operatorId id of operator
     * @param value      value
     * @return an expression
     */
    public String matcherCondition(String functionId,
                                   Map<String, String> bindings,
                                   String operatorId,
                                   String value) {
        var fct = dictionary.getFunction(functionId);
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
        var operator = dictionary.getOperator(operatorId);
        return String.join(" ", fctInvocation, operator.getName(), format(value, operator));
    }


    private String format(String value, Typed definition) {
        if (definition.needQuotes()) {
            return "'" + value + "'"; // TODO: handle escaping
        } else {
            return value;
        }
    }

}
