package com.neterium.client.sdk.screening;

import com.neterium.client.sdk.binding.JetScanBinder;
import com.neterium.client.sdk.matching.MatchVerifier;
import com.neterium.client.sdk.model.ScreenableParty;
import com.neterium.client.sdk.properties.SdkProperties;
import com.neterium.client.sdk.throttling.Throttler;
import com.neterium.sdk.api.JetscanApi;
import com.neterium.sdk.model.CoreRequestContext;
import com.neterium.sdk.model.CoreScreenOptions;
import com.neterium.sdk.model.CoreScreenOptionsResponseContent;
import com.neterium.sdk.model.JetScanRequestBody;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;

/**
 * A screener of counterparts.
 * Entities to screen may be provided in two different ways
 * <ul>
 * <li>as a collection of {@link ScreenableParty} entities</li>
 * <li>as a{@link JetScanRequestBody} instance pre-filled with records</li>
 * </ul>
 *
 * @author Bernard Ligny
 */
@Component
@Slf4j
public class CounterpartScreener extends BaseScreener {

    private static final String DURATION_METRIC = "neterium.jetscan.durations";
    private static final String RECORD_METRIC = "neterium.jetscan.counterparts";

    private final JetscanApi jetscanApi;
    private final JetScanBinder jetScanBinder;
    private final MatchVerifier matchVerifier;

    /**
     * Constructor
     *
     * @param jetscanApi    a {@link JetscanApi} instance
     * @param throttler     a {@link Throttler} instance
     * @param meterRegistry a <code>MeterRegistry</code> instance
     * @param jetScanBinder a {@link JetScanBinder} instance
     * @param matchVerifier a {@link MatchVerifier} instance
     * @param sdkProperties SDK configuration
     */
    public CounterpartScreener(JetscanApi jetscanApi,
                               Throttler throttler,
                               MeterRegistry meterRegistry,
                               JetScanBinder jetScanBinder,
                               MatchVerifier matchVerifier,
                               SdkProperties sdkProperties) {
        super(throttler, sdkProperties);
        this.jetscanApi = jetscanApi;
        this.jetScanBinder = jetScanBinder;
        this.matchVerifier = matchVerifier;
        this.durationMetric = Timer.builder(DURATION_METRIC)
                .description("JetScan request durations")
                .publishPercentileHistogram(true)
                .distributionStatisticExpiry(Duration.ofHours(1))
                .publishPercentiles(0.5, 0.8, 0.9, 0.95, 0.99)
                .register(meterRegistry);
        this.recordMetric = Counter.builder(RECORD_METRIC)
                .description("Number of screened counterparts")
                .register(meterRegistry);
    }


    /**
     * Screen a collection of counterparts
     *
     * @param request   a generic screening request containing the entities to screen,
     *                  as well as the name of the collection to screen against
     * @param threshold the screening threshold
     * @param sessionId session identifier to use in subsequent API calls
     * @return a "high-level" screening response
     * TODO: replace threshold param with a generic option builder
     */
    public ScreeningResponse doScreen(ScreeningRequest<? extends ScreenableParty> request, int threshold, String sessionId) {
        var jetScanRequestBody = buildJetScanRequest(request, threshold, sessionId);
        if (log.isTraceEnabled()) {
            log.trace("...jetScanRequest :: {}", jetScanRequestBody);
        }
        return doScreen(jetScanRequestBody, request.getCollectionName());
    }


    /**
     * Invoke JetScan screening API using the provided native request
     *
     * @param jetScanRequestBody a pre-filled native JetScan screening request
     * @param collectionName     name of the collection to screen against
     * @return a "high-level" screening response
     */
    public ScreeningResponse doScreen(JetScanRequestBody jetScanRequestBody, String collectionName) {
        int size = jetScanRequestBody.getRecords().size();
        log.info("About to screen batch of {} counterpart(s) against '{}'", size, collectionName);
        var start = Instant.now();
        try {
            var jetScanResponse = jetscanApi.screen(collectionName, jetScanRequestBody, super.validatePayload());
            var elapsed = Duration.between(start, Instant.now());
            throttler.registerDuration(elapsed);
            durationMetric.record(elapsed);
            recordMetric.increment(size);
            if (log.isTraceEnabled()) {
                log.trace("...jetScanResponse :: {}", jetScanResponse);
            }
            log.info("Screening outcome: {} hits on {} screened records ({})",
                    jetScanResponse.getStats().getHitRecords(),
                    jetScanResponse.getStats().getScreenedRecords(),
                    jetScanResponse.getStats().getElapsed());
            return ScreeningResponse.of(jetScanResponse, matchVerifier);
        } catch (RestClientException exception) {
            var elapsed = Duration.between(start, Instant.now());
            durationMetric.record(elapsed);
            throw handleException(exception, elapsed, jetScanRequestBody);
        }
    }


    // TODO: replace threshold param with a generic option builder
    private JetScanRequestBody buildJetScanRequest(ScreeningRequest<? extends ScreenableParty> request,
                                                   int threshold,
                                                   String sessionId) {
        return jetScanBinder.bind(request)
                .context(
                        new CoreRequestContext()
                                .sessionId(sessionId)
                                .reference(request.getReference())
                                .clientReference(screeningProperties.getClientReference())
                )
                .options(
                        new CoreScreenOptions()
                                .responseContent(
                                        new CoreScreenOptionsResponseContent()
                                                .listContext(false)
                                                .screeningStats(true)
                                                .profileSummary(true)
                                )
                                .threshold(threshold)
                );
    }

}
