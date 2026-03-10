package com.neterium.client.sdk.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * A base abstract class to build custom iterators
 *
 * @param <T> the type of elements returned by this iterator
 * @author Bernard Ligny
 */
public abstract class IteratorSupport<T> implements Iterator<T> {

    protected T nextValue;

    /**
     * Constructor
     */
    protected IteratorSupport() {
    }

    /**
     * @see Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        advance();
        return nextValue != null;
    }


    /**
     * @see Iterator#next()
     */
    @Override
    public T next() {
        return Optional.ofNullable(nextValue)
                .orElseThrow(NoSuchElementException::new);
    }


    private void advance() {
        nextValue = readNextValue();
    }

    protected abstract T readNextValue();

}

