package com.neterium.client.sdk.matching;

import com.neterium.client.sdk.model.ScreenedObjectType;
import com.neterium.sdk.model.CoreResponseMatch;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Default {@link MatchVerifier} pass-through implementation which accepts every match.
 *
 * @author Bernard Ligny
 */
@Component
@ConditionalOnMissingBean(MatchVerifier.class)
public class MatchVerifierPassThroughImpl implements MatchVerifier {

    /**
     * Constructor
     */
    public MatchVerifierPassThroughImpl() {
    }

    /**
     * @see MatchVerifier#verify(CoreResponseMatch, ScreenedObjectType, String)
     */
    @Override
    public boolean verify(CoreResponseMatch match, ScreenedObjectType objectType, String objectRef) {
        return true;
    }

}
