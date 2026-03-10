package com.neterium.client.sdk.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * ApiProperties
 *
 * @author Bernard Ligny
 */
@Getter
@Setter
public class JobProperties {

    /**
     * Maximum number of retries (after initial attempt)
     */
    private int maxRetries = 5;

}
