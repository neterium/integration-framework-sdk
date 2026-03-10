package com.neterium.client.sdk.matching;

import com.neterium.client.sdk.model.ScreenedObjectType;
import com.neterium.sdk.model.CoreResponseMatch;

/**
 * Abstraction of the logic to verify a candidate match.
 * When negative, candidate match is discarded.
 *
 * @author Bernard Ligny
 */
@FunctionalInterface
public interface MatchVerifier {

    /**
     * Verify that a candidate match (found by Neterium API) is relevant
     *
     * @param match      candidate match
     * @param objectType whether the match to find relates to a counterpart or a transaction
     * @param objectRef  reference of the related counterpart/transaction
     * @return false to discard, true to keep it
     */
    boolean verify(CoreResponseMatch match, ScreenedObjectType objectType, String objectRef);

}
