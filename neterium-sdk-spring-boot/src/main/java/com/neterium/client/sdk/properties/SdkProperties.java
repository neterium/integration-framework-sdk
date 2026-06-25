package com.neterium.client.sdk.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * SdkProperties
 *
 * @author Bernard Ligny
 */
@ConfigurationProperties(prefix = "neterium")
@Getter
@Setter
public class SdkProperties {

    /**
     * A map of (client-id, client-secret) pairs
     */
    private Map<String, Credentials> apiKeys = new HashMap<>();

    @NestedConfigurationProperty
    private ThrottlingProperties throttling = new ThrottlingProperties();

    @NestedConfigurationProperty
    private ApiProperties apiServer = new ApiProperties();

    @NestedConfigurationProperty
    private HttpClientProperties httpClient = new HttpClientProperties();

    @NestedConfigurationProperty
    private OAuth2Properties oauth2 = new OAuth2Properties();

    @NestedConfigurationProperty
    private TokenProperties tokens = new TokenProperties();

    @NestedConfigurationProperty
    private ScreeningProperties screening = new ScreeningProperties();

    @NestedConfigurationProperty
    private JobProperties jobs = new JobProperties();

    @NestedConfigurationProperty
    private ExceptionProperties exceptions = new ExceptionProperties();


    /**
     * Credentials
     *
     * @param clientId     client id
     * @param clientSecret client secret
     */
    public record Credentials(String clientId, String clientSecret) {
    }

}
