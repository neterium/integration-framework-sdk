package com.neterium.client.sdk.throttling;

import com.neterium.client.sdk.instrumentation.Measurable;
import com.neterium.client.sdk.properties.SdkProperties;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generic throttler implementation.
 * <p>
 * Parameters:
 * <ul>
 *   <li>Duration windowSize (S)</li>
 *      <li>Metric metric (M)</li>
 *      <li>int windowMinCount (N)</li>
 *      <li>Duration thresholdValue (T)</li>
 *      <li>double thresholdRatio (R)</li>
 *      <li>int timeoutMaxOccurrences (C)</li>
 *      <li>double variationAmount (P)</li>
 *  </ul>
 * Each time a new duration is registered, after a warm-up phase (at least N registrations)
 * compute a metric M (for instance: mean time) on a sliding window of S seconds,
 * and adjust the throttler value using the following logic:
 * <ul>
 *   <li>if the upper limit (T) is reached, the throttler value is scaled down by P</li>
 *   <li>if the lower limit (T * R%) is reached, the throttler value is left unchanged</li>
 *   <li>if under both limits, the throttler value is scaled up by P</li>
 * </ul>
 * If more than C timeouts occur within the sliding window, the throttler value is reset
 * to its initial value.
 * <p>
 * In case of quota excess, the throttler value is reduced to its minimum size
 *
 * @author Bernard Ligny
 */
@Component
@Slf4j
public class ThrottlerImpl implements Throttler, Measurable {

    private static final String VALUE_METRIC = "neterium.throttler";

    private final List<ThrottlerListener> listeners = new ArrayList<>();
    private final Object lock = new Object();

    private final Duration windowSize; // param (S)
    private final Metric metric; // param (M)
    private final int windowMinCount; // param (N)
    private final Duration thresholdValue; // param (T)
    private final double thresholdRatio; // param (R)
    private final int timeoutMaxOccurrences; // param (C)
    private final double variationAmount; // param (P)
    private final int initialValue;
    private final int minValue;
    private final int maxValue;

    // Statistics on recent request
    private final TimeBasedSlidingStatistics durationStatistics;
    private final TimeBasedSlidingStatistics timeoutStatistics;

    // Current (calibrated) value
    private double throttlerValue;


    /**
     * Constructor
     *
     * @param meterRegistry a <code>MeterRegistry</code> instance
     * @param sdkProperties SDK configuration
     */
    public ThrottlerImpl(MeterRegistry meterRegistry, SdkProperties sdkProperties) {
        var conf = sdkProperties.getThrottling();
        this.windowSize = conf.getWindow().getSize();
        this.metric = conf.getMetric().getName();
        this.windowMinCount = conf.getMetric().getMinCount();
        this.thresholdValue = conf.getDurations().getThresholdValue();
        this.thresholdRatio = conf.getDurations().getThresholdRatio();
        this.timeoutMaxOccurrences = conf.getTimeouts().getMaxOccurrences();
        this.variationAmount = conf.getCalibration().getVariationAmount();
        this.initialValue = conf.getCalibration().getInitialValue();
        this.minValue = conf.getCalibration().getMinValue();
        this.maxValue = conf.getCalibration().getMaxValue();
        durationStatistics = new TimeBasedSlidingStatistics(windowSize);
        timeoutStatistics = new TimeBasedSlidingStatistics(windowSize);
        Gauge.builder(VALUE_METRIC, () -> throttlerValue)
                .description("Throttler value")
                .register(meterRegistry);
        init();
    }


    /**
     * @see Throttler#registerDuration(Duration)
     */
    @Override
    public void registerDuration(Duration duration) {
        log.trace("Registering new duration: {} ms", duration.toMillis());
        durationStatistics.addValue(duration.toMillis());
        log.info("Stats for last {} ::= {}", windowSize, durationStatistics);
        if (durationStatistics.getAggregatedCount() < windowMinCount) {
            log.trace("Warm-up phase, waiting for at least {} durations", windowMinCount);
            return;
        }
        boolean modified = false;
        if (metric.getValue(durationStatistics) < lowerLimit()) {
            // Increase value when under LOWER limit
            modified = updateValueBy(variationAmount);
        } else if (metric.getValue(durationStatistics) >= upperLimit()) {
            // Decrease value when over UPPER limit
            modified = updateValueBy(variationAmount * -1);
        }
        if (!modified) {
            log.trace("Throttler value left unchanged : {}", throttlerValue);
        }
    }


    /**
     * @see Throttler#registerTimeOut(Duration)
     */
    @Override
    public void registerTimeOut(Duration duration) {
        registerDuration(duration);
        timeoutStatistics.addValue(Instant.now().toEpochMilli());
        int nbTimeouts = timeoutStatistics.getValueCount();
        log.debug("Timeout Count ::= {}", nbTimeouts);
        if (nbTimeouts > timeoutMaxOccurrences) {
            log.warn("Too many timeouts ({}), reset threshold back to initial value", timeoutMaxOccurrences);
            updateValueTo(initialValue, false);
        }
    }


    /**
     * @see Throttler#registerQuotaExceeded()
     */
    @Override
    public void registerQuotaExceeded() {
        if (throttlerValue > minValue) {
            updateValueTo(minValue, true);
        }
    }


    /**
     * @see Throttler#addListener(ThrottlerListener)
     */
    @Override
    public void addListener(ThrottlerListener listener) {
        listeners.add(listener);
    }


    /**
     * @see Measurable#describe()
     */
    @Override
    public Map<String, Object> describe() {
        var snapshot = durationStatistics.getSnapshot();
        return Map.of(
                // Parameters
                "window-size", windowSize.toSeconds(),
                "lower-limit", (long) lowerLimit(),
                "upper-limit", (long) upperLimit(),
                "metric-name", metric.name(),
                // Measures
                "value-count", (long) snapshot.size(),
                "min-value", snapshot.getMin(),
                "max-value", snapshot.getMax(),
                "metric-value", metric.getValue(snapshot).longValue(),
                "throttler-value", (long) throttlerValue,
                "timeouts", (long) timeoutStatistics.getValueCount()
        );
    }


    /**
     * @see Measurable#getCategoryName()
     */
    @Override
    public String getCategoryName() {
        return "throttling";
    }


    // === Private stuff ===


    private void init() {
        throttlerValue = initialValue;
        log.info("Using metric {} on window of size {}s with threshold {}ms and initial throttler value of {}",
                metric.name(), windowSize.toSeconds(), thresholdValue.toMillis(), throttlerValue);
    }


    private double lowerLimit() {
        return Math.round(thresholdValue.toMillis() * thresholdRatio);
    }


    private double upperLimit() {
        return thresholdValue.toMillis();
    }


    private boolean updateValueBy(double delta) {
        synchronized (lock) {
            return updateValueTo(throttlerValue + delta, false);
        }
    }


    private boolean updateValueTo(double newValue, boolean forceValue) {
        if (withinBoundaries(newValue)) {
            int previousDiscreteValue, newDiscreteValue;
            synchronized (lock) {
                previousDiscreteValue = (int) Math.floor(throttlerValue);
                if (!forceValue) {
                    throttlerValue = Math.max(initialValue, Math.min(newValue, maxValue));
                }
                log.trace("Throttler value updated to: {}", throttlerValue);
                newDiscreteValue = (int) Math.floor(throttlerValue);
            }
            if (newDiscreteValue != previousDiscreteValue) {
                listeners.forEach(
                        l -> l.onValueUpdated(
                                this.getClass().getName(), previousDiscreteValue, newDiscreteValue)
                );
            }
            return true;
        } else {
            return false;
        }
    }


    private boolean withinBoundaries(Double value) {
        return (value > minValue) && (value < maxValue);
    }

}
