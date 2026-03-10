package com.neterium.client.sdk.model;

import com.neterium.client.sdk.matching.MatchVerifier;
import lombok.NonNull;

/**
 * Ability to be disproved.
 * Disproval is considered repeatable as long as object (ie checksum) is unchanged
 *
 * @author Bernard Ligny
 * @see MatchVerifier
 */
public interface Refutable {

    /**
     * Test whether object has been disproved
     *
     * @return true if disproved, false otherwise
     */
    boolean isDisproved();

    /**
     * Test whether object is unchanged, by comparing its current checksum
     * with the provided checksum
     *
     * @param checkSum the checksum to compare with
     * @return true if checksums are equals
     */
    boolean hasCheckSum(@NonNull Number checkSum);

}
