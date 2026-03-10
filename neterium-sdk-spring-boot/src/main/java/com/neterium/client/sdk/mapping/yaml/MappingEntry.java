package com.neterium.client.sdk.mapping.yaml;

import lombok.Data;

import java.util.List;
import java.util.Optional;

/**
 * An entry of the {@link Mappings} holding the necessary info
 * to populate a target field of a given type
 * by applying a function on a list of multiple sources
 *
 * @author Bernard Ligny
 */
@Data
public class MappingEntry {

    private DataType type = DataType.STRING;
    private MappingFunction function = MappingFunction.FIRST;
    private List<String> sources;
    private String fragment;
    private List<String> values;

    /**
     * Get the owning fragment.
     * If none is returned, entry will be considered as attached to the primary fragment
     *
     * @return fragment name
     */
    public Optional<String> getFragment() {
        return Optional.ofNullable(fragment);
    }

}
