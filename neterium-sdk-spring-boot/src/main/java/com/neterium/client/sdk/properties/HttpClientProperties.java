package com.neterium.client.sdk.properties;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

/**
 * HttpClientProperties
 *
 * @author Bernard Ligny
 */
@Getter
@Setter
public class HttpClientProperties {

    /**
     * Connection timeout
     */
    private Duration connectTimeout = Duration.ofSeconds(30);

    /**
     * Read (socket) timeout
     */
    private Duration readTimeout = Duration.ofSeconds(60);

    /**
     * Pool size
     */
    private int poolSize = 25;

}
