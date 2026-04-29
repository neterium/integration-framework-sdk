package com.neterium.client.sdk.exbuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry
 *
 * @author Bernard Ligny
 */
@Data
public class Entry {

    @JsonProperty("id")
    private int id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("title")
    private String title;

    @JsonProperty("formatType")
    private String type;

    @JsonProperty("exceptionCategory")
    private int categoryId;

    @JsonProperty("format")
    private JsonNode format;

    @JsonProperty("syntax")
    private List<String> syntaxes = new ArrayList<>();

    @JsonProperty("jetscan")
    private boolean jetScan;

    @JsonProperty("jetflow")
    private boolean jetFlow;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append("title", title)
                .append("type", type)
                .append("syntax", syntaxes)
                .toString();
    }

}
