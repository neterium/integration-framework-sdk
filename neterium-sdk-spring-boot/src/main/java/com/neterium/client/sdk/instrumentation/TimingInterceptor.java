package com.neterium.client.sdk.instrumentation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * A {@link ClientHttpRequestInterceptor} implementation that can be used
 * by a Spring rest client to measure to durations of issued HTTP requests
 *
 * @author Bernard Ligny
 */
@Slf4j
public class TimingInterceptor implements ClientHttpRequestInterceptor {

    private final int latencyMin;
    private final int latencyMax;

    /**
     * Default constructor
     */
    public TimingInterceptor() {
        this(-1, -1);
    }


    /**
     * Alternate constructor with fake random network latency (for benchmark purposes)
     *
     * @param latencyMin minimum latency (in ms)
     * @param latencyMax maximum latency (in ms)
     */
    public TimingInterceptor(int latencyMin, int latencyMax) {
        this.latencyMin = latencyMin;
        this.latencyMax = latencyMax;
    }


    /**
     * @see ClientHttpRequestInterceptor#intercept(HttpRequest, byte[], ClientHttpRequestExecution)
     */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        long start = System.currentTimeMillis();
        ClientHttpResponse response = execution.execute(request, body);
        var delay = randomLatency();
        long duration = System.currentTimeMillis() - start;
        if (delay > 0) {
            log.trace("{} {} : {} ms (including fake latency of {} ms}", request.getMethod(), request.getURI(), duration, delay);
        } else {
            log.trace("{} {} : {} ms", request.getMethod(), request.getURI(), duration);
        }
        return response;
    }


    private int randomLatency() {
        if (latencyMin > 0) {
            var latency = ThreadLocalRandom.current().nextInt(latencyMin, latencyMax);
            try {
                TimeUnit.MILLISECONDS.sleep(latency);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return latency;
        } else {
            return 0;
        }
    }


}
