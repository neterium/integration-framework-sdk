package com.neterium.client.sdk.security;

import com.neterium.client.sdk.properties.SdkProperties;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Lazy {@link TokenService}  implementation where tokens are requested <strong>on demand</strong>
 * (when effectively needed) avoiding thereby unnecessary refresh.
 *
 * @author Bernard Ligny
 */
@Service
@ConditionalOnProperty(prefix = "neterium.tokens", name = "eager", havingValue = "false")
@Slf4j
public class TokenServiceLazyImpl extends BaseTokenServiceImpl {

    // Custom extra claim to store token issue date
    private static final String CLAIM_NAME = "_issueDate";


    /**
     * Constructor
     *
     * @param oauth2Client  an {@link OAuth2Client} instance
     * @param cacheManager  a {@link CacheManager} instance
     * @param sdkProperties SDK configuration
     */
    public TokenServiceLazyImpl(OAuth2Client oauth2Client,
                                CacheManager cacheManager,
                                SdkProperties sdkProperties) {
        super(oauth2Client, cacheManager, sdkProperties);
    }


    /**
     * @see BaseTokenServiceImpl#onNewToken(String, JSONObject)
     */
    @Override
    protected void onNewToken(String cacheKey, JSONObject oauth2Token) {
        // Enrich token with an additional claim
        oauth2Token.put(CLAIM_NAME, LocalDateTime.now());
    }


    /**
     * @see BaseTokenServiceImpl#isExpired(JSONObject, String)
     */
    @Override
    protected boolean isExpired(JSONObject oauth2Token, String expirationClaim) {
        var expiresInSeconds = oauth2Token.getInt(expirationClaim);
        LocalDateTime issueDate = (LocalDateTime) oauth2Token.get(CLAIM_NAME);
        LocalDateTime expirationDateTime = issueDate
                .plusSeconds(expiresInSeconds)
                // Safety margin: invalidate token x seconds before limit
                .minusSeconds(super.getExpirationOffsetInSec());
        return LocalDateTime.now().isAfter(expirationDateTime);
    }


}
