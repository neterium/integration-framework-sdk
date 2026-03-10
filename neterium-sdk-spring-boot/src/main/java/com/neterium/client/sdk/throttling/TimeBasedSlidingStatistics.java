package com.neterium.client.sdk.throttling;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.SlidingTimeWindowArrayReservoir;
import com.codahale.metrics.Snapshot;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A sliding window that stores only the measurements made in the last N seconds
 *
 * @author Bernard Ligny
 * @see SlidingTimeWindowArrayReservoir
 */
public class TimeBasedSlidingStatistics {

    private final Histogram histogram;

    /**
     * Constructor
     *
     * @param width width of window
     */
    public TimeBasedSlidingStatistics(Duration width) {
        var reservoir = new SlidingTimeWindowArrayReservoir(width.toSeconds(), TimeUnit.SECONDS);
        this.histogram = new Histogram(reservoir);
    }

    /**
     * Register a value
     *
     * @param value the value to register
     */
    public void addValue(long value) {
        histogram.update(value);
    }

    /**
     * Get the number of values present in the window
     *
     * @return number of elements
     */
    public int getValueCount() {
        return getSnapshot().size();
    }

    /**
     * Get the total number of registered values since the beginning
     *
     * @return aggregated number of elements
     */
    public long getAggregatedCount() {
        return histogram.getCount();
    }

    /**
     * Get a snapshot with all values of the window
     *
     * @return a <code>Snapshot</code> instance
     */
    public Snapshot getSnapshot() {
        return histogram.getSnapshot();
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        var snapshot = getSnapshot();
        var data = Map.of(
                "count", (double) snapshot.size(),
                "min", (double) snapshot.getMin(),
                "max", (double) snapshot.getMax(),
                "average", snapshot.getMean()
        );
        return data.toString();
    }

}
