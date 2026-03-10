package com.neterium.client.sdk.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A helper to efficiently group large volumes of data into batches of N elements
 *
 * @author Bernard Ligny
 */
public class BatchHelper {


    /**
     * Disable instantiation
     */
    private BatchHelper() {
    }


    /**
     * Group a stream of data into batches
     *
     * @param stream    the stream of data
     * @param batchSize the desired batch size
     * @param <T>       the type of the stream elements
     * @return a stream of batches
     */
    public static <T> Stream<List<T>> groupIntoBatches(Stream<T> stream, int batchSize) {
        return groupIntoBatches(stream, batchSize, false);
    }


    /**
     * Group a stream of data into batches
     *
     * @param stream    the stream of data
     * @param batchSize the desired batch size
     * @param parallel  whether to returned stream is a parallel (true) or a sequential (false) stream
     * @param <T>       the type of the stream elements
     * @return a stream of batches
     */
    public static <T> Stream<List<T>> groupIntoBatches(Stream<T> stream, int batchSize, boolean parallel) {
        var iterator = stream.iterator();
        var spliterator = new Spliterators.AbstractSpliterator<List<T>>(Long.MAX_VALUE, Spliterator.ORDERED) {
            @Override
            public boolean tryAdvance(Consumer<? super List<T>> action) {
                var batch = new ArrayList<T>(batchSize);
                var i = 0;
                while (i++ < batchSize && iterator.hasNext()) {
                    batch.add(iterator.next());
                }
                if (batch.isEmpty()) return false;
                action.accept(batch);
                return true;
            }
        };
        return StreamSupport.stream(spliterator, parallel)
                .onClose(stream::close);
    }

}
