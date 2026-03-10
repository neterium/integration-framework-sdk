package com.neterium.client.sdk.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * ScreeningProperties
 *
 * @author Bernard Ligny
 */
@Getter
@Setter
public class ScreeningProperties {

    /**
     * Enable (server-side) validation of submitted screening payloads.
     * Set it to false in order to improve response times
     */
    private boolean validate = true;
    
}
