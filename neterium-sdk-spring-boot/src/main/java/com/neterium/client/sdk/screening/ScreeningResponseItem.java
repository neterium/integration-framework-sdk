package com.neterium.client.sdk.screening;

import com.neterium.sdk.model.CoreResponseItem;
import com.neterium.sdk.model.CoreResponseMatch;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * Generic screening response item encapsulating
 * <ul>
 * <li>the raw screening result for a single item</li>
 * <li>the found alerts, that is to say <strong>non-discarded</strong> matches</li>
 * </ul>
 *
 * @author Bernard Ligny
 */
@AllArgsConstructor
@NoArgsConstructor
public class ScreeningResponseItem {

    @Getter
    private CoreResponseItem screenerOutcome;

    private Collection<CoreResponseMatch> alerts;


    /**
     * Get the found matches
     *
     * @param filterOut whether to filter out matches to exclude discarded ones
     * @return a collection of matches
     */
    public Collection<CoreResponseMatch> getMatches(boolean filterOut) {
        return (filterOut ? this.alerts : this.screenerOutcome.getMatches());
    }

    /**
     * Test whether non-discarded matches exist
     *
     * @return true if non-discarded matches exist, false otherwise
     */
    public boolean hasAlert() {
        return !alerts.isEmpty();
    }

    /**
     * Get the number of non-discarded matches
     *
     * @return number of non-discarded matches
     */
    public int getAlertCount() {
        return alerts.size();
    }

}
