package com.neterium.client.sdk.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * OAuth2Properties
 *
 * @author Bernard Ligny
 */
@Getter
@Setter
public class OAuth2Properties {

    /**
     * Uri of endpoint to fetch openId configuration (eg ../.well-known/openid-configuration)
     */
    private String wellKnownUri;

    /**
     * Client id to use in OAUth2 authentication handshake
     */
    private String clientId;
    
}
