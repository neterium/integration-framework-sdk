package com.neterium.client.sdk.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neterium.client.sdk.instrumentation.TimingInterceptor;
import com.neterium.client.sdk.properties.ApiProperties;
import com.neterium.client.sdk.properties.HttpClientProperties;
import com.neterium.client.sdk.properties.SdkProperties;
import com.neterium.client.sdk.security.TokenService;
import com.neterium.sdk.ApiClient;
import com.neterium.sdk.RFC3339DateFormat;
import com.neterium.sdk.api.*;
import jakarta.annotation.PreDestroy;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.text.DateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Configuration of the java proxies allowing to invoke Neterium rest API
 *
 * @author Bernard Ligny
 */
@Configuration(proxyBeanMethods = false)
@Import({TimingInterceptor.class})
public class ApiClientConfig {

    private final TokenService tokenService;
    private final ApiProperties apiProperties;
    private final HttpClientProperties httpProperties;


    /**
     * Constructor
     *
     * @param tokenService  a {@link TokenService} instance
     * @param sdkProperties SDK configuration
     */
    public ApiClientConfig(TokenService tokenService, SdkProperties sdkProperties) {
        this.tokenService = tokenService;
        this.apiProperties = sdkProperties.getApiServer();
        this.httpProperties = sdkProperties.getHttpClient();
    }


    /**
     * Expose a pre-configured {@link ApiClient} instance
     *
     * @param builder           a <code>RestClient.Builder</code> instance
     * @param objectMapper      a <code>ObjectMapper</code> instance
     * @param timingInterceptor a {@link TimingInterceptor} instance
     * @return a {@link ApiClient} instance
     */
    @Bean
    public ApiClient apiClient(RestClient.Builder builder,
                               ObjectMapper objectMapper,
                               TimingInterceptor timingInterceptor) {
        var restClient = builder.requestInterceptor(timingInterceptor)
                .requestFactory(customClientHttpRequestFactory())
                .build();
        var apiClient = new ApiClient(restClient, objectMapper, defaultDateFormat())
                .setBasePath(apiProperties.getBaseUrl());
        apiClient.setBearerToken(() -> tokenService.getApiToken(apiProperties.getKeyId()));
        return apiClient;
    }


    /**
     * Expose a ready-to-use instance of {@link JetscanApi} proxy
     *
     * @param apiClient a {@link ApiClient} instance
     * @return a {@link JetscanApi} instance
     */
    @Bean
    public JetscanApi jetScanApi(ApiClient apiClient) {
        return new JetscanApi(apiClient);
    }


    /**
     * Expose a ready-to-use instance of {@link JetflowApi} proxy
     *
     * @param apiClient a {@link ApiClient} instance
     * @return a {@link JetflowApi} instance
     */
    @Bean
    public JetflowApi jetFlowApi(ApiClient apiClient) {
        return new JetflowApi(apiClient);
    }


    /**
     * Expose a ready-to-use instance of {@link SessionApi} proxy
     *
     * @param apiClient a {@link ApiClient} instance
     * @return a {@link SessionApi} instance
     */
    @Bean
    public SessionApi sessionApi(ApiClient apiClient) {
        return new SessionApi(apiClient);
    }

    /**
     * Expose a ready-to-use instance of {@link RepositoryApi} proxy
     *
     * @param apiClient a {@link ApiClient} instance
     * @return a {@link RepositoryApi} instance
     */
    @Bean
    public RepositoryApi repositoryApi(ApiClient apiClient) {
        return new RepositoryApi(apiClient);
    }


    /**
     * Expose a ready-to-use instance of {@link ExceptionsApi} proxy
     *
     * @param apiClient a {@link ApiClient} instance
     * @return a {@link ExceptionsApi} instance
     */
    @Bean
    public ExceptionsApi exceptionsApi(ApiClient apiClient) {
        return new ExceptionsApi(apiClient);
    }


    /**
     * Expose a ready-to-use instance of {@link ListsApi} proxy
     *
     * @param apiClient a {@link ApiClient} instance
     * @return a {@link ListsApi} instance
     */
    @Bean
    public ListsApi listsApi(ApiClient apiClient) {
        return new ListsApi(apiClient);
    }


    /**
     * Perform ultimate logout on shut down
     */
    @PreDestroy
    public void onShutdown() {
        tokenService.logout(apiProperties.getKeyId());
    }


    /**
     * Sadly, not all Apache HttpClient settings are exposed as Spring properties.
     * Furthermore, HttpClient defaults are quite low for high-concurrency applications.
     * So let's configure it by hand.
     */
    private ClientHttpRequestFactory customClientHttpRequestFactory() {
        var connConfig = ConnectionConfig.custom()
                .setConnectTimeout(httpProperties.getConnectTimeout().getSeconds(), TimeUnit.SECONDS)
                .setSocketTimeout((int) httpProperties.getReadTimeout().getSeconds(), TimeUnit.SECONDS)
                .build();
        var reqConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(httpProperties.getConnectTimeout().getSeconds(), TimeUnit.SECONDS)
                .setResponseTimeout(httpProperties.getReadTimeout().getSeconds(), TimeUnit.SECONDS)
                .build();
        var connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(httpProperties.getPoolSize());
        connectionManager.setDefaultMaxPerRoute(httpProperties.getPoolSize());
        connectionManager.setDefaultConnectionConfig(connConfig);
        var httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(reqConfig)
                .build();
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }


    private DateFormat defaultDateFormat() {
        var dateFormat = new RFC3339DateFormat();
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat;
    }

}
