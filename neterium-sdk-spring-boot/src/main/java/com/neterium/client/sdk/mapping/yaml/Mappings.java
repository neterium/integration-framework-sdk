package com.neterium.client.sdk.mapping.yaml;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A structure representing the YAML content of a mapping definition file :
 * <ul>
 * <li>names of the fragment (node) to consider</li>
 * <li>mapping rules</li>
 * </ul>
 *
 * @author Bernard Ligny
 */
@Data
public class Mappings {

    private List<Fragment> fragments;
    private Map<String, MappingEntry> mappings;


    /**
     * Get the primary fragment
     *
     * @return the primary fragment
     */
    public String getPrimaryFragment() {
        return fragments.stream()
                .filter(Fragment::isPrimary)
                .findFirst()
                .map(Fragment::getName)
                .orElseThrow(NoSuchElementException::new);
    }


    /**
     * Get all registered fragments
     *
     * @return a set of fragment names
     */
    public Set<String> getFragmentNames() {
        return fragments.stream()
                .map(Fragment::getName)
                .collect(Collectors.toSet());
    }

}
