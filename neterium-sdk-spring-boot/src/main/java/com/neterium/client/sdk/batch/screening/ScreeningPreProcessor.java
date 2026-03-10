package com.neterium.client.sdk.batch.screening;

import com.neterium.client.sdk.model.Screenable;
import com.neterium.client.sdk.screening.ScreeningResponseItem;
import org.springframework.batch.item.ItemProcessor;

/**
 * An <code>ItemProcessor</code> implementation that is preparing the screening phase
 * by turning a {@link Screenable} item into a 2-tuple with
 * <ul>
 *  <li>the original item</li>
 *  <li>a blank response item</li>
 * </ul>
 *
 * @param <T> type of screened objects
 * @author Bernard Ligny
 */
public class ScreeningPreProcessor<T extends Screenable>
        implements ItemProcessor<T, ScreeningTuple<T>> {

    /**
     * Constructor
     */
    public ScreeningPreProcessor() {
    }


    /**
     * Turn input item into a {@link ScreeningTuple} holding it
     */
    @Override
    public ScreeningTuple<T> process(T item) {
        return new ScreeningTuple<>(item, new ScreeningResponseItem());
    }

}
