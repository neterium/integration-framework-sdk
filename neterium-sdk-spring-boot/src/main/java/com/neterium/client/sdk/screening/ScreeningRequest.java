package com.neterium.client.sdk.screening;

import com.neterium.client.sdk.model.Screenable;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Generic screening request encapsulating
 * <ul>
 * <li>the collection of items to screen</li>
 * <li>screening parameters (TODO: add builder for usual screening options)</li>
 * <li>a unique reference for this request</li>
 * </ul>
 *
 * @param <T> type of screened items
 * @author Bernard Ligny
 */
@Getter
public class ScreeningRequest<T extends Screenable> {

    private final String collectionName;
    private final List<T> items;
    private final String reference;


    /**
     * Constructor
     *
     * @param collectionName collection to screen against
     * @param items          the items to screen
     */
    public ScreeningRequest(String collectionName,
                            Collection<? extends T> items) {
        this.collectionName = collectionName;
        this.items = new ArrayList<>(items);
        this.reference = generateReference();
    }


    /**
     * Get the request size in terms if request items
     *
     * @return number of items
     */
    public int size() {
        return items.size();
    }


    /**
     * Human friendly summary of this request
     *
     * @return a string
     */
    @Override
    public String toString() {
        return getReference() + " (" + size() + " items)";
    }


    private String generateReference() {
        return Long.toHexString(UUID.randomUUID().getMostSignificantBits());
    }

}
