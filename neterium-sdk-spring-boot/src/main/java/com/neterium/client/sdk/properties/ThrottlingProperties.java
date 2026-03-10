package com.neterium.client.sdk.properties;

import com.neterium.client.sdk.throttling.Metric;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

/**
 * ThrottlingProperties
 *
 * @author Bernard Ligny
 */
@Getter
@Setter
public class ThrottlingProperties {

    private WindowProperties window = new WindowProperties();
    private CalibrationProperties calibration = new CalibrationProperties();
    private DurationProperties durations = new DurationProperties();
    private MetricProperties metric = new MetricProperties();
    private TimeoutProperties timeouts = new TimeoutProperties();


    /**
     * WindowProperties
     */
    @Getter
    @Setter
    public static class WindowProperties {

        /**
         * Size (duration) of the window
         */
        private Duration size = Duration.ofSeconds(60);
    }


    /**
     * CalibrationProperties
     */
    @Getter
    @Setter
    public static class CalibrationProperties {

        /**
         * Incrementation/decrementation step when adjusting throttler value
         * (between 0 et 1)
         */
        private double variationAmount = 0.1d;

        /**
         * Throttler initial value
         */
        private int initialValue = 3;

        /**
         * Throttler minimal value
         */
        private int minValue = 1;

        /**
         * Throttler maximum value
         */
        private int maxValue = 10;
    }


    /**
     * DurationProperties
     */
    @Getter
    @Setter
    public static class DurationProperties {

        /**
         * Upper limit value for durations of computed metric
         */
        private Duration thresholdValue = Duration.ofSeconds(30);

        /**
         * Factor to apply on upper limit value to compute lower limit value
         */
        private double thresholdRatio = 0.9d;
    }


    /**
     * MetricProperties
     */
    @Getter
    @Setter
    public static class MetricProperties {

        /**
         * Computed metric (see Metric enum)
         */
        private Metric name = Metric.MEAN;

        /**
         * Count of captured metric values to consider the warm-up phase as over
         */
        private int minCount = 10;
    }


    /**
     * TimeoutProperties
     */
    @Getter
    @Setter
    public static class TimeoutProperties {

        /**
         * Maximum occurrences of timeouts (within window.size duration)
         * before reverting throttler back to its initial value
         */
        private int maxOccurrences = 5;

    }

}
