package com.neterium.client.sdk.batch.screening;

import com.neterium.client.sdk.model.Screenable;
import com.neterium.client.sdk.screening.ScreeningResponseItem;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Models a pair
 * <ul>
 *  <li>a {@link Screenable} object</li>
 *  <li>the results of screening this object</li>
 * </ul>
 *
 * @param <T> type of screened object
 * @author Bernard Ligny
 */
public class ScreeningTuple<T extends Screenable> {

    private final Pair pair;

    /**
     * Constructor
     *
     * @param input  the objet being screened
     * @param result the screening result
     */
    public ScreeningTuple(T input, ScreeningResponseItem result) {
        this.pair = new Pair(input, result);
    }

    /**
     * Get screened object
     *
     * @return the screened object
     */
    public T getInput() {
        return pair.input;
    }

    /**
     * Get screening result
     *
     * @return the screening result
     */
    public ScreeningResponseItem getResult() {
        return pair.result;
    }

    /**
     * Set screening result
     *
     * @param result the screening result
     */
    public void setResult(ScreeningResponseItem result) {
        pair.result = result;
    }


    @Data
    @AllArgsConstructor
    private class Pair {
        final T input;
        ScreeningResponseItem result;
    }

}
