package com.neterium.client.sdk.exbuilder;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.commons.lang3.StringUtils;
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


    public String buildPayload(String rawExpression, String reference, String profileId) {
        var factory = JsonNodeFactory.instance;
        var node = factory.objectNode();
        node.put("reference", reference);
        if (StringUtils.isNotEmpty(profileId)) {
            node.put("profileId", profileId);
            node.put("expirationType", "PROFILE");
        } else {
            node.put("expirationType", "NEVER");
        }
        var conditions = node.putArray("conditions");
        conditions.add(StringUtils.normalizeSpace(rawExpression));
        return node.toPrettyString();
    }


    private String format(String value, Typed definition) {
        if (definition.needQuotes()) {
            return "'" + value + "'"; // TODO: escaping ?
        } else {
            return value;
        }
    }


}
