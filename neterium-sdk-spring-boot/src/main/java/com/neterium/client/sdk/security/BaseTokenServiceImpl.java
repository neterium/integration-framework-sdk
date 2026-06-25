package com.neterium.client.sdk.security;

import com.neterium.client.sdk.properties.SdkProperties;
import com.neterium.client.sdk.properties.SdkProperties.Credentials;
import com.neterium.client.sdk.properties.TokenProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.cache.CacheManager;

import java.util.Map;
import java.util.Optional;

import static com.neterium.client.sdk.configuration.CacheConfig.TOKEN_CACHE;

/**
 * Base class for {@link TokenService} implementations, which is
 * <ul>
 *   <li>supporting potentially multiple API keys</li>
 *   <li>offering primitives to get or renew OAuth2 tokens</li>
 *   <li>allowing caching of obtained tokens</li>
 * </ul>
 * Concrete implementations can use these building blocks to manage the token renewal
 *
 * @author Bernard Ligny
 */
@Slf4j
public abstract class BaseTokenServiceImpl implements TokenService {

    private final OAuth2Client oauth2Client;
    private final CacheManager cacheManager;
    private final TokenProperties tokenProperties;
    private final Map<String, Credentials> allApiKeys;


    /**
     * Constructor
     */
    protected BaseTokenServiceImpl(OAuth2Client oauth2Client,
                                   CacheManager cacheManager,
                                   SdkProperties sdkProperties) {
        this.oauth2Client = oauth2Client;
        this.cacheManager = cacheManager;
        this.tokenProperties = sdkProperties.getTokens();
        this.allApiKeys = sdkProperties.getApiKeys();

    }

    /**
     * Prefetch all configured tokens on startup
     */
    @PostConstruct
    private void preFetchTokens() {
        if (tokenProperties.isPrefetch()) {
            log.info("Pre-fetching tokens ({} found)", allApiKeys.size());
            allApiKeys.keySet().forEach(this::getApiToken);
        }
    }


    /**
     * @see TokenService#getApiToken(String)
     */
    @Override
    public String getApiToken(String keyId) {
        var credentials = allApiKeys.get(keyId);
        if (credentials == null) {
            log.error("Invalid key {}", keyId);
            throw new IllegalArgumentException("Requested API key (" + keyId + ") was not found");
        }
        return getOrReuseApiToken(keyId, credentials.clientId(), credentials.clientSecret());
    }


    /**
     * @see TokenService#getApiToken(String, String)
     */
    @Override
    public String getApiToken(String clientId, String clientSecret) {
        // Use clientId as cache key
        return getOrReuseApiToken(clientId, clientId, clientSecret);
    }


    /**
     * @see TokenService#logout(String)
     */
    @Override
    public boolean logout(String keyOrClientId) {
        JSONObject found = getFromCache(keyOrClientId);
        if (found != null) {
            return oauth2Client.logout(found.getString("id_token"));
        } else {
            return false;
        }
    }


    /**
     * React on any received authentication token
     *
     * @param cacheKey    a unique identifier that can be used for caching
     * @param oauth2Token the newly obtained OAuth2 token
     */
    protected void onNewToken(String cacheKey, JSONObject oauth2Token) {
    }


    /**
     * Test whether an authentication token is expired by examining an expiration claim
     *
     * @param oauth2Token     an OAuth2 token
     * @param expirationClaim name of expiration claim
     * @return true if expired, false otherwise
     */
    protected abstract boolean isExpired(JSONObject oauth2Token, String expirationClaim);


    // === Building blocks ===


    protected int getExpirationOffsetInSec() {
        return tokenProperties.getExpirationOffsetSec();
    }


    protected JSONObject renewOAuth2Token(String cacheKey, String clientId, String refreshToken) {
        JSONObject newOAuth2Token = oauth2Client.refreshToken(clientId, refreshToken);
        handleNewToken(cacheKey, newOAuth2Token);
        return newOAuth2Token;
    }


    // === Private ===


    private String getOrReuseApiToken(String cacheKey, String clientId, String clientSecret) {
        var oauth2Token = getFromCache(cacheKey);
        if (oauth2Token != null) {
            log.debug("Cache hit for '{}'", cacheKey);
            if (isExpired(oauth2Token, "expires_in")) {
                log.warn("Expired access token for '{}'", cacheKey);
                if (isExpired(oauth2Token, "refresh_expires_in")) {
                    log.warn("Expired refresh token for '{}'", cacheKey);
                    oauth2Token = null; // invalidate token
                } else {
                    oauth2Token = renewOAuth2Token(cacheKey, clientId, oauth2Token.getString("refresh_token"));
                }
            }
        }
        if (oauth2Token == null) {
            log.debug("Requesting new token for '{}'", cacheKey);
            oauth2Token = oauth2Client.getNewToken(clientId, clientSecret);
            handleNewToken(cacheKey, oauth2Token);
        }
        return oauth2Token.getString("access_token");
    }


    private void handleNewToken(String cacheKey, JSONObject oauth2Token) {
        log.trace("Handling new token: {}", oauth2Token);
        onNewToken(cacheKey, oauth2Token);
        putInCache(cacheKey, oauth2Token);
        log.trace("Caching token for '{}'", cacheKey);
    }


    private JSONObject getFromCache(String cacheKey) {
        var cache = cacheManager.getCache(TOKEN_CACHE);
        assert cache != null;
        return Optional.ofNullable(cache.get(cacheKey))
                .map(entry -> (JSONObject) entry.get())
                .orElse(null);
    }


    private void putInCache(String cacheKey, JSONObject cacheValue) {
        var cache = cacheManager.getCache(TOKEN_CACHE);
        assert cache != null;
        cache.put(cacheKey, cacheValue);
    }

}
