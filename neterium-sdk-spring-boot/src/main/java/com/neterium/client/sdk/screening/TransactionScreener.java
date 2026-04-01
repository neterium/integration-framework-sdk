package com.neterium.client.sdk.screening;

import com.neterium.client.sdk.binding.JetFlowBinder;
import com.neterium.client.sdk.matching.MatchVerifierPassThroughImpl;
import com.neterium.client.sdk.model.ScreenableTransaction;
import com.neterium.client.sdk.properties.SdkProperties;
import com.neterium.client.sdk.throttling.Throttler;
import com.neterium.sdk.api.JetflowApi;
import com.neterium.sdk.model.CoreRequestContext;
import com.neterium.sdk.model.CoreScreenOptions;
import com.neterium.sdk.model.JetFlowRequestBody;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;

/**
 * A screener of transactions.
 * Entities to screen may be provided in two different ways
 * <ul>
 * <li>as a collection of {@link ScreenableTransaction} entities</li>
 * <li>as a{@link JetFlowRequestBody} instance pre-filled with records</li>
 * </ul>
 *
 * @author Bernard Ligny
 */
@Component
@Slf4j
public class TransactionScreener extends BaseScreener {

    private static final String DURATION_METRIC = "neterium.jetflow.durations";
    private static final String RECORD_METRIC = "neterium.jetflow.counterparts";

    private final JetflowApi jetflowApi;
    private final JetFlowBinder jetFlowBinder;


    /**
     * Constructor
     *
     * @param jetflowApi    a {@link JetflowApi} instance
     * @param throttler     a {@link Throttler} instance
     * @param meterRegistry a <code>MeterRegistry</code> instance
     * @param jetFlowBinder a {@link JetFlowBinder} instance
     * @param sdkProperties SDK configuration
     */
    public TransactionScreener(JetflowApi jetflowApi,
                               Throttler throttler,
                               MeterRegistry meterRegistry,
                               JetFlowBinder jetFlowBinder,
                               SdkProperties sdkProperties) {
        super(throttler, sdkProperties);
        this.jetflowApi = jetflowApi;
        this.jetFlowBinder = jetFlowBinder;
        this.durationMetric = Timer.builder(DURATION_METRIC)
                .description("JetFlow request durations")
                .publishPercentileHistogram(true)
                .distributionStatisticExpiry(Duration.ofHours(1))
                .publishPercentiles(0.5, 0.8, 0.9, 0.95, 0.99)
                .register(meterRegistry);
        this.recordMetric = Counter.builder(RECORD_METRIC)
                .description("Number of screened transactions")
                .register(meterRegistry);
    }


    /**
     * Screen a collection of transactions
     *
     * @param request   a generic screening request containing the entities to screen,
     *                  as well as the name of the collection to screen against
     * @param threshold the screening threshold
     * @param sessionId session identifier to use in subsequent API calls
     * @return a "high-level" screening response
     * TODO: replace threshold param with a generic option builder
     */
    public ScreeningResponse doScreen(ScreeningRequest<? extends ScreenableTransaction<?>> request, int threshold, String sessionId) {
        var jetFlowRequestBody = buildJetFlowRequest(request, threshold, sessionId);
        if (log.isTraceEnabled()) {
            log.trace("...jetFlowRequest :: {}", jetFlowRequestBody);
        }
        return doScreen(jetFlowRequestBody, request.getCollectionName());
    }


    /**
     * Invoke JetFlow screening API using the provided native request
     *
     * @param jetFlowRequestBody a pre-filled native JetFlow screening request
     * @param collectionName     name of the collection to screen against
     * @return a "high-level" screening response
     */
    public ScreeningResponse doScreen(JetFlowRequestBody jetFlowRequestBody, String collectionName) {
        int size = jetFlowRequestBody.getRecords().size();
        log.info("About to screen batch of {} transaction(s) against '{}'", size, collectionName);
        var start = Instant.now();
        try {
            var jetFlowResponse = jetflowApi.jfscreen(collectionName, jetFlowRequestBody, super.validatePayload());
            var elapsed = Duration.between(start, Instant.now());
            throttler.registerDuration(elapsed);
            durationMetric.record(elapsed);
            recordMetric.increment(size);
            if (log.isTraceEnabled()) {
                log.trace("...jetFlowResponse :: {}", jetFlowResponse);
            }
            log.info("Screening outcome: {} hits on {} screened records ({})",
                    jetFlowResponse.getStats().getHitRecords(),
                    jetFlowResponse.getStats().getScreenedRecords(),
                    jetFlowResponse.getStats().getElapsed());
            return ScreeningResponse.of(jetFlowResponse, new MatchVerifierPassThroughImpl());
        } catch (RestClientException exception) {
            var elapsed = Duration.between(start, Instant.now());
            durationMetric.record(elapsed);
            throw handleException(exception, elapsed, jetFlowRequestBody);
        }
    }


    // TODO: replace threshold param with a generic option builder
    private JetFlowRequestBody buildJetFlowRequest(ScreeningRequest<? extends ScreenableTransaction<?>> request,
                                                   int threshold,
                                                   String sessionId) {
        return jetFlowBinder.bind(request)
                .options(
                        new CoreScreenOptions()
                                .threshold(threshold)
                )
                .context(
                        new CoreRequestContext()
                                .sessionId(sessionId)
                                .reference(request.getReference())
                                .clientReference(screeningProperties.getClientReference())
                );
    }

}
