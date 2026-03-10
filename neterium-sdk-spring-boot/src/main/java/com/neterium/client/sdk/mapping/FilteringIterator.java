package com.neterium.client.sdk.mapping;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.neterium.client.sdk.utils.IteratorSupport;

import java.io.IOException;
import java.util.Set;

/**
 * A utility class to make a {@link JsonParser} iterate on specific nodes only
 * (based on node names)
 *
 * @author Bernard Ligny
 */
public class FilteringIterator extends IteratorSupport<FilteringIterator.Entry> {

    private final JsonParser parser;
    private final Set<String> fragments;


    /**
     * Constructor
     *
     * @param parser    JsonParser to use for filtering nodes
     * @param fragments the collection of node names to retain
     */
    public FilteringIterator(JsonParser parser, Set<String> fragments) {
        this.parser = parser;
        this.fragments = fragments;
    }


    /**
     * @see IteratorSupport#readNextValue()
     */
    @Override
    protected Entry readNextValue() {
        try {
            String tokenName = "?";
            while (parser.nextToken() != null) {
                JsonToken token = parser.currentToken();
                if (token == JsonToken.FIELD_NAME) {
                    tokenName = parser.currentName();
                } else if (token == JsonToken.START_OBJECT && fragments.contains(tokenName)) {
                    return new Entry(tokenName, parser.readValueAsTree());
                }
            }
            return null;
        } catch (IOException e) {
            throw new RuntimeException("Parsing error", e);
        }
    }


    /**
     * A record holding a retained JSON node
     *
     * @param name the node name
     * @param node the node content
     */
    public record Entry(String name, TreeNode node) {
    }

}

