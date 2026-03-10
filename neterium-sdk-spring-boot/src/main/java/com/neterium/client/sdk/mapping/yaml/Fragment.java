package com.neterium.client.sdk.mapping.yaml;

import lombok.Data;

/**
 * The "Fragment" definition inside a {@link Mappings}
 *
 * @author Bernard Ligny
 */
@Data
public class Fragment {

    private String name;
    private boolean primary = false;

}
