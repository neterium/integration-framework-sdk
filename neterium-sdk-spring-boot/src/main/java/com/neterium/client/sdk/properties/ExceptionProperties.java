package com.neterium.client.sdk.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * ExceptionProperties
 *
 * @author Bernard Ligny
 */
@Getter
@Setter
public class ExceptionProperties {

    /**
     * Indicates which expiration policy is to be used for created exceptions
     * <ul>
     *     <li>true : expire when core checksum changes</li>
     *     <li>false : expire when full checksum changes</li>
     * </ul>
     */
    private boolean useCoreCheckSum = false;
    
}
