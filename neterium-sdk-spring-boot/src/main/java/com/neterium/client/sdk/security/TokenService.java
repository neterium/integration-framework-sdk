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
     * @param username username
     * @param password user password
     * @return an API access token
     */
    String getApiToken(String username, String password);


    /**
     * Logout (end session)
     *
     * @param keyOrUsername logical key or username
     * @param isKey         whether the provided string is a key (true) or a username (false)
     * @return true if logout succeeded, false otherwise
     */
    boolean logout(String keyOrUsername, boolean isKey);

}
