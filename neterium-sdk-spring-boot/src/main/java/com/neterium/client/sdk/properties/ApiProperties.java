package com.neterium.client.sdk.properties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * All configuration properties of the SDK
 *
 * @author Bernard Ligny
 */
@Getter
@Setter
@NoArgsConstructor
public class ApiProperties {

    /**
     * Base url of target server
     */
    private String baseUrl;
    /**
     * Reference to one of the "api-keys" to use for authentication
     */
    private String keyId;

}
