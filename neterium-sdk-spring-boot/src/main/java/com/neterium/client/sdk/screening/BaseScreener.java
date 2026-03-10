package com.neterium.client.sdk.screening;

import com.neterium.client.sdk.exception.FatalException;
import com.neterium.client.sdk.exception.RetryableException;
import com.neterium.client.sdk.exception.SkippableException;
import com.neterium.client.sdk.instrumentation.Measurable;
import com.neterium.client.sdk.properties.ScreeningProperties;
import com.neterium.client.sdk.properties.SdkProperties;
import com.neterium.client.sdk.throttling.Throttler;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Abstract screener associated to a {@link Throttler}
 * that will be used to register response times and timeouts of issued screening requests
 *
 * @author Bernard Ligny
 */
@Slf4j
public abstract class BaseScreener implements Measurable {

    protected final Throttler throttler;
    protected final ScreeningProperties screeningProperties;

    protected Timer durationMetric;
    protected Counter recordMetric;


    /**
     * Constructor
     */
    protected BaseScreener(Throttler throttler, SdkProperties sdkProperties) {
        this.throttler = throttler;
        this.screeningProperties = sdkProperties.getScreening();
    }


    /**
     * @see Measurable#getCategoryName()
     */
    @Override
    public String getCategoryName() {
        return "screening";
    }


    /**
     * @see Measurable#describe()
     */
    @Override
    public Map<String, Object> describe() {
        var snapshot = durationMetric.takeSnapshot();
        var p90 = Arrays.stream(snapshot.percentileValues())
                .filter(p -> p.percentile() == 0.9)
                .findFirst()
                .map(p -> p.value(TimeUnit.MILLISECONDS))
                .map(Double::longValue)
                .orElse(0L);
        return Map.of(
                "count-records", (long) recordMetric.count(),
                "count-requests", snapshot.count(),
                "time-average", (long) snapshot.mean(TimeUnit.MILLISECONDS),
                "time-p90", p90,
                "time-unit", "ms");
    }


    /**
     * Log statistics when component is shut down
     */
    @PreDestroy
    private void shutdown() {
        log.info("{} statistics: {}", this.getClass().getSimpleName(), this.describe());
    }


    /**
     * Is payload validation needed for issued screening requests ?
     */
    protected boolean validatePayload() {
        return screeningProperties.isValidate();
    }


    /**
     * Handle a REST exception, i.e. make {@link Throttler} aware
     *
     * @param exception   The REST exception to handle
     * @param elapsed     the elapsed time
     * @param requestBody body of screening request
     */
    protected RuntimeException handleException(RestClientException exception,
                                               Duration elapsed,
                                               Object requestBody) {
        return switch (exception) {
            case RestClientResponseException e -> handleResponseFailure(e, elapsed, requestBody);
            case ResourceAccessException e -> handleClientFailure(e, elapsed, requestBody);
            default -> new FatalException(exception);
        };
    }


    private RuntimeException handleResponseFailure(RestClientResponseException exception,
                                                   Duration elapsed,
                                                   Object requestBody) {
        switch (exception.getStatusCode()) {
            case HttpStatus.GATEWAY_TIMEOUT:
                log.warn("Gateway timeout ({} ms) with request {}", elapsed.toMillis(), requestBody);
                throttler.registerTimeOut(elapsed);
                return new RetryableException("Gateway timeout", exception);
            case HttpStatus.TOO_MANY_REQUESTS:
                log.warn("Quota exceeded with request {}", requestBody);
                throttler.registerQuotaExceeded();
                return new SkippableException("Quota exceeded", exception);
            default:
                log.error("Server-side web error", exception);
                return new FatalException(exception);
        }
    }


    private RuntimeException handleClientFailure(ResourceAccessException exception,
                                                 Duration elapsed,
                                                 Object requestBody) {
        switch (exception.getCause()) {
            case HttpTimeoutException e -> {
                log.warn("Read timeout ({} ms) with request {}", elapsed.toMillis(), requestBody);
                throttler.registerTimeOut(elapsed);
                return new RetryableException("Read timeout", exception);
            }
            case SocketTimeoutException e -> {
                log.warn("Socket timeout ({} ms) with request {}", elapsed.toMillis(), requestBody);
                throttler.registerTimeOut(elapsed);
                return new RetryableException("Read timeout", exception);
            }
            case SocketException e -> {
                log.warn("Socket error with request {}", requestBody);
                return new RetryableException("Read error", exception);
            }
            case null, default -> {
                log.error("Client-side web error", exception);
                return new FatalException(exception);
            }
        }
    }

}
