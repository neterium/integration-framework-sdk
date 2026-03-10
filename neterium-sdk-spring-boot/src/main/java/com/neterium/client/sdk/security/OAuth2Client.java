package com.neterium.client.sdk.security;

import com.neterium.client.sdk.exception.SdkException;
import com.neterium.client.sdk.properties.OAuth2Properties;
import com.neterium.client.sdk.properties.SdkProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Client of an OAuth2-compliant server using Spring <code>RestClient</code>
 *
 * @author Bernard Ligny
 */
@Component
@Slf4j
@ConditionalOnProperty("neterium.oauth2.well-known-uri")
public class OAuth2Client {

    private final RestClient restClient;
    private final OAuth2Properties oAuth2Properties;

    private String tokenEndpointUri;
    private String endSessionEndpointUri;


    /**
     * Constructor
     *
     * @param builder       a <code>RestClient.Builder</code> instance
     * @param sdkProperties SDK configuration
     */
    public OAuth2Client(RestClient.Builder builder, SdkProperties sdkProperties) {
        this.oAuth2Properties = sdkProperties.getOauth2();
        // Use specific User-Agent for nice display in Keycloak console
        // (apparently not everything is accepted :-/)
        this.restClient = builder.defaultHeader(HttpHeaders.USER_AGENT, "Apache-HttpClient/4.5.13 (Java/" +
                        System.getProperty("java.version") + ")")
                .build();
    }


    /**
     * Load OpenID configuration using the configured "Well-known" URI
     */
    @PostConstruct
    public void getOpenIdConfig() {
        log.trace("Loading OpenID configuration from {}", oAuth2Properties.getWellKnownUri());
        var response = restClient.get()
                .uri(oAuth2Properties.getWellKnownUri())
                .retrieve()
                .toEntity(String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            var config = new JSONObject(response.getBody());
            tokenEndpointUri = config.getString("token_endpoint");
            log.trace("TokenEndpointUri is: {}", tokenEndpointUri);
            endSessionEndpointUri = config.getString("end_session_endpoint");
            log.trace("EndSessionEndpointUri is: {}", endSessionEndpointUri);
        } else {
            var errorBody = response.getBody();
            log.error("Unable to load OpenID configuration: {}", errorBody);
            throw new SdkException(errorBody);
        }
    }


    /**
     * Get an OAuth2 token using password credentials
     *
     * @param username : user name
     * @param password : user password
     * @return an OAuth2 token
     */
    public JSONObject getNewToken(String username, String password) {
        return invokeTokenEndpoint("password", Map.of(
                "password", password,
                "username", username,
                "client_id", oAuth2Properties.getClientId(),
                "scope", "openid profile email")
        );
    }


    /**
     * Get an OAuth2 token using a refresh token
     *
     * @param refreshToken : a refresh token
     * @return an OAuth2 token
     */
    public JSONObject refreshToken(String refreshToken) {
        return invokeTokenEndpoint("refresh_token", Map.of(
                "client_id", oAuth2Properties.getClientId(),
                "refresh_token", refreshToken)
        );
    }


    /**
     * Relying-Party (ie user) Initiated Logout
     *
     * @param idToken id of token to logout
     * @return true if logout successful, false otherwise
     */
    public boolean logout(String idToken) {
        log.trace("Invoking endpoint {}", endSessionEndpointUri);
        var uri = UriComponentsBuilder
                .fromUriString(endSessionEndpointUri)
                .queryParam("id_token_hint", idToken)
                .build()
                .toUri();
        var response = restClient.get()
                .uri(uri)
                .retrieve()
                .toEntity(String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            log.trace("HttpResponse status  ::= {}", response.getStatusCode());
            return true;
        } else {
            throw new SdkException(response.getBody());
        }
    }


    /**
     * Invoke the OAuth2 token endpoint
     *
     * @param grantType  : the grant type to use in the OAuth2 flow
     * @param parameters : a map of parameters to use in the HTTP request body
     */
    private JSONObject invokeTokenEndpoint(String grantType, Map<String, String> parameters) {
        var map = new LinkedMultiValueMap<>();
        map.add("grant_type", grantType);
        parameters.forEach(map::add);
        log.trace("Invoking endpoint {} with parameters: {}", tokenEndpointUri, map);
        var response = restClient.post()
                .uri(tokenEndpointUri)
                .body(map)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .retrieve()
                .toEntity(String.class);
        log.trace("HttpResponse status  ::= {}", response.getStatusCode());
        if (response.getStatusCode().is2xxSuccessful()) {
            return new JSONObject(response.getBody());
        } else {
            throw new SdkException(response.getBody());
        }
    }

}
