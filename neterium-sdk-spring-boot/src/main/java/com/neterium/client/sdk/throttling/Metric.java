package com.neterium.client.sdk.throttling;

import com.codahale.metrics.Snapshot;

import java.util.function.Function;

/**
 * Available metrics that can be used to calibrate the throttler value.
 * The computed metric value will be compared to the configured threshold value.
 *
 * @author Bernard Ligny
 */
public enum Metric {

    /**
     * Count of values
     */
    COUNT(snapshot -> (double) snapshot.size()),

    /**
     * Mean of values
     */
    MEAN(Snapshot::getMean),

    /**
     * Median value
     */
    MEDIAN(Snapshot::getMedian),

    /**
     * Min value
     */
    MIN(snapshot -> (double) snapshot.getMin()),

    /**
     * Max value
     */
    MAX(snapshot -> (double) snapshot.getMax()),

    /**
     * Standard Deviation
     */
    STD_DEV(Snapshot::getStdDev),

    /**
     * Percentile 60
     */
    PERCENTILE_60(snapshot -> snapshot.getValue(60)),

    /**
     * Percentile 70
     */
    PERCENTILE_70(snapshot -> snapshot.getValue(70)),

    /**
     * Percentile 75
     */
    PERCENTILE_75(Snapshot::get75thPercentile),

    /**
     * Percentile 80
     */
    PERCENTILE_80(snapshot -> snapshot.getValue(80)),

    /**
     * Percentile 85
     */
    PERCENTILE_85(snapshot -> snapshot.getValue(85)),

    /**
     * Percentile 90
     */
    PERCENTILE_90(snapshot -> snapshot.getValue(90)),

    /**
     * Percentile 95
     */
    PERCENTILE_95(Snapshot::get95thPercentile),

    /**
     * Percentile 98
     */
    PERCENTILE_98(Snapshot::get98thPercentile),

    /**
     * Percentile 99
     */
    PERCENTILE_99(Snapshot::get99thPercentile);


    private final Function<Snapshot, Double> function;


    /**
     * Constructor
     *
     * @param function the function to use to compute metric value
     */
    Metric(Function<Snapshot, Double> function) {
        this.function = function;
    }


    /**
     * Compute this metrics for all values inside a window
     *
     * @param window a {@link TimeBasedSlidingStatistics} window containing measurements
     * @return the computed metric value
     */
    public Double getValue(TimeBasedSlidingStatistics window) {
        return getValue(window.getSnapshot());
    }


    /**
     * Compute this metrics for all values present in a snapshot
     *
     * @param snapshot a {@link Snapshot} of values
     * @return the computed metric value
     */
    public Double getValue(Snapshot snapshot) {
        return function.apply(snapshot);
    }

}
