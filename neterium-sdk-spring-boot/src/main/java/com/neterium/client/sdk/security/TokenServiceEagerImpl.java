package com.neterium.client.sdk.security;

import com.neterium.client.sdk.properties.SdkProperties;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Eager {@link TokenService} implementation where tokens are <strong>proactively</strong> renewed
 * in case of potential future requests.
 *
 * @author Bernard Ligny
 */
@Service
@ConditionalOnProperty(prefix = "neterium.tokens", name = "eager", havingValue = "true", matchIfMissing = true)
@Slf4j
public class TokenServiceEagerImpl extends BaseTokenServiceImpl {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);

    /**
     * Constructor
     *
     * @param oauth2Client  an {@link OAuth2Client} instance
     * @param cacheManager  a {@link CacheManager} instance
     * @param sdkProperties SDK configuration
     */
    public TokenServiceEagerImpl(OAuth2Client oauth2Client,
                                 CacheManager cacheManager,
                                 SdkProperties sdkProperties) {
        super(oauth2Client, cacheManager, sdkProperties);
    }


    /**
     * Shutdown this component
     */
    @PreDestroy
    private void onShutDown() {
        scheduler.shutdownNow();
    }


    /**
     * @see BaseTokenServiceImpl#onNewToken(String, JSONObject)
     */
    @Override
    protected void onNewToken(String cacheKey, JSONObject oauth2Token) {
        if (!scheduler.isShutdown()) {
            scheduleAutoRenewal(cacheKey, oauth2Token);
        }
    }


    /**
     * @see BaseTokenServiceImpl#isExpired(JSONObject, String)
     */
    @Override
    protected boolean isExpired(JSONObject oauth2Token, String expirationClaim) {
        // By definition, tokens are always valid as renewed on time
        return false;
    }


    /**
     * Schedule the renewal of a OAuth2 token using its refresh token.
     *
     * @param cacheKey    to key to use for caching
     * @param oauth2Token the OAuth2 token to renew
     */
    private void scheduleAutoRenewal(String cacheKey, JSONObject oauth2Token) {
        Runnable task = () -> {
            log.debug("Refreshing token for '{}'", cacheKey);
            var clientId = oauth2Token.getString("client_id");
            var refreshToken = oauth2Token.getString("refresh_token");
            super.renewOAuth2Token(cacheKey, clientId, refreshToken);
        };
        // Safety margin: renew token x seconds before limit
        var expiresInSeconds = oauth2Token.getInt("expires_in") - getExpirationOffsetInSec();
        scheduler.schedule(task, expiresInSeconds, TimeUnit.SECONDS);
        var expirationDate = LocalDateTime.now().plusSeconds(expiresInSeconds);
        log.debug("Scheduling token renewal for '{}' on {}", cacheKey, expirationDate);
    }

}
