package com.neterium.client.sdk.binding;

import com.neterium.client.sdk.model.Screenable;
import com.neterium.client.sdk.screening.ScreeningRequest;

import java.util.Collection;

/**
 * Model the ability to bind some screenable data to a target model
 *
 * @param <F> the type of source beans
 * @param <T> the type of the target beans
 * @author Bernard Ligny
 */
public interface Binder<F extends Screenable, T> {

    /**
     * Bind a {@link ScreeningRequest} of {@link Screenable} objects
     *
     * @param request a screening request containing the elements to bind
     * @return an instance of T containing the binding results
     */
    T bind(ScreeningRequest<? extends F> request);

    /**
     * Bind a collection of {@link Screenable} objects
     *
     * @param data the elements to bind
     * @return an instance of T containing the binding results
     */
    T bind(Collection<? extends F> data);

}
