package com.neterium.client.sdk.properties;

import lombok.Getter;
import lombok.Setter;

/**
 * TokenProperties
 *
 * @author Bernard Ligny
 */
@Getter
@Setter
public class TokenProperties {

    /**
     * Set to true for proactive renewal (just before expired),
     * set to false for on-demand renewal
     */
    private boolean eager = false;

    /**
     * Pre-fetch a token on application startup
     */
    private boolean prefetch = false;

    /**
     * Consider tokens as invalid X seconds before expiry
     */
    private int expirationOffsetSec = 5;

}
