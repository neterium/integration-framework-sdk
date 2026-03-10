package com.neterium.client.sdk.binding;

import com.neterium.client.sdk.model.BaseEnum;
import com.neterium.client.sdk.model.Screenable;
import com.neterium.client.sdk.screening.ScreeningRequest;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Base class for a {@link Binder} implementation with a {@link Screenable} input
 *
 * @param <F> the type of source beans
 * @param <T> the type of the target beans
 * @author Bernard Ligny
 */
public abstract class BaseBinder<F extends Screenable, T> implements Binder<F, T> {


    /**
     * Constructor
     */
    protected BaseBinder() {
    }

    /**
     * @see Binder#bind(ScreeningRequest)
     */
    @Override
    public T bind(ScreeningRequest<? extends F> request) {
        return bind(request.getItems());
    }


    /**
     * Handle the mapping of enums
     */
    protected <V extends Enum<?>> V mapEnum(BaseEnum sourceEnum, Function<String, V> builder) {
        return Optional.ofNullable(sourceEnum)
                .map(value -> builder.apply(value.getInternalCode()))
                .orElse(null);
    }


    /**
     * Replace null value with empty string
     */
    protected String nvl(String s) {
        return Objects.toString(s, "");
    }


}
