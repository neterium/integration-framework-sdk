package com.neterium.client.sdk.matching;

import com.neterium.client.sdk.model.Refutable;
import com.neterium.client.sdk.model.ScreenedObjectType;
import com.neterium.sdk.model.CoreResponseMatch;
import com.neterium.sdk.model.CoreResponseProfileSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * {@link MatchVerifier} implementation that executes some client-side queries to perform the validation.
 * Will replace the default impl ({@link MatchVerifierPassThroughImpl} )
 * as soon as a {@link MatchFinder} bean is found in Spring registry
 *
 * @author Bernard Ligny
 */
@Component
@ConditionalOnBean(MatchFinder.class)
@Slf4j
public class MatchVerifierClientImpl implements MatchVerifier {

    private final MatchFinder<? extends Refutable> matchFinder;

    /**
     * Constructor
     *
     * @param matchFinder the MatchFinder instance to use in the verification process
     */
    public MatchVerifierClientImpl(MatchFinder<? extends Refutable> matchFinder) {
        this.matchFinder = matchFinder;
    }


    /**
     * @see MatchVerifier#verify(CoreResponseMatch, ScreenedObjectType, String)
     */
    @Override
    public boolean verify(CoreResponseMatch match, ScreenedObjectType objectType, String objectRef) {
        var checksum = Optional.ofNullable(match.getProfileSummary())
                .map(CoreResponseProfileSummary::getChecksum)
                .orElse(BigDecimal.ZERO);
        log.trace("Looking for existing match on {} with checksum {}", match.getProfileId(), checksum);
        var disproved = matchFinder.findByTypeAndRefAndProfile(objectType, objectRef, match.getProfileId())
                .filter(existingMatch -> existingMatch.hasCheckSum(checksum))
                .map(Refutable::isDisproved)
                .orElse(false);
        if (disproved) {
            log.warn("Ignoring match for profileId {}", match.getProfileId());
            return false;
        } else {
            return true;
        }
    }

}
