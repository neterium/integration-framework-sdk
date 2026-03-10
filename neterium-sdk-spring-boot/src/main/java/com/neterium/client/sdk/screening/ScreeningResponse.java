package com.neterium.client.sdk.screening;

import com.neterium.client.sdk.matching.MatchVerifier;
import com.neterium.client.sdk.model.ScreenedObjectType;
import com.neterium.sdk.model.ScreenResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic screening response holding a list of pairs with:
 * <ul>
 * <li>a reference to screened object</li>
 * <li>the corresponding screening results</li>
 * </ul>
 *
 * @author Bernard Ligny
 */
public class ScreeningResponse {

    private final Map<String, ScreeningResponseItem> resultsMap = new HashMap<>();


    /**
     * Disable instantiation
     */
    private ScreeningResponse() {
    }


    /**
     * Factory method
     *
     * @param response      the complete screening response (for the whole batch)
     * @param matchVerifier a {@link MatchVerifier} instance used to filter out matches
     * @return a {@link ScreeningResponse} istance
     */
    public static ScreeningResponse of(ScreenResponse response, MatchVerifier matchVerifier) {
        var instance = new ScreeningResponse();
        for (var next : response.getResults()) {
            var relevantMatches = next.getMatches()
                    .stream()
                    .filter(match -> matchVerifier.verify(match, ScreenedObjectType.COUNTERPART, next.getReference()))
                    .toList();
            var sri = new ScreeningResponseItem(next, relevantMatches);
            instance.resultsMap.put(next.getReference(), sri);
        }
        return instance;
    }


    /**
     * Get the screening results for a specific input
     *
     * @param reference reference to screened object
     * @return the corresponding {@link ScreeningResponseItem}, or null if none is found
     */
    public ScreeningResponseItem getByReference(String reference) {
        return resultsMap.get(reference);
    }


    /**
     * Human friendly summary of this request
     *
     * @return a string
     */
    @Override
    public String toString() {
        return resultsMap.toString();
    }


}
