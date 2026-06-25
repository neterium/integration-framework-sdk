package com.neterium.client.sdk.security;

/**
 * Provider of ready-to-use (ie always valid) API tokens
 *
 * @author Bernard Ligny
 */
public interface TokenService {

    /**
     * Get a ready-to-use API token for a specific logical key
     * that will be used to fetch appropriate credentials
     *
     * @param keyId logical key identifier
     * @return an API access token
     */
    String getApiToken(String keyId);


    /**
     * Get a ready-to-use API token based on credentials
     *
     * @param clientId     : client id
     * @param clientSecret : client secret
     * @return an API access token
     */
    String getApiToken(String clientId, String clientSecret);


    /**
     * Logout (end session)
     *
     * @param keyOrClientId logical key identifier
     * @return true if logout succeeded, false otherwise
     */
    boolean logout(String keyOrClientId);

}
