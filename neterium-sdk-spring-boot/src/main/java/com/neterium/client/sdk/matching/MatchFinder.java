package com.neterium.client.sdk.matching;

import com.neterium.client.sdk.model.Refutable;
import com.neterium.client.sdk.model.ScreenedObjectType;

import java.util.Optional;

/**
 * Abstraction of the logic to find refutable matches
 *
 * @param <T> a {@link Refutable} class
 * @author Bernard Ligny
 */
@FunctionalInterface
public interface MatchFinder<T extends Refutable> {

    /**
     * Find an existing match by search criteria
     *
     * @param objectType whether the match to find relates to a counterpart or a transaction
     * @param objectRef  identifier of the related counterpart/transaction
     * @param profileId  reference of the matched profile
     * @return the found existing match if any
     */
    Optional<T> findByTypeAndRefAndProfile(ScreenedObjectType objectType, String objectRef, String profileId);

}
