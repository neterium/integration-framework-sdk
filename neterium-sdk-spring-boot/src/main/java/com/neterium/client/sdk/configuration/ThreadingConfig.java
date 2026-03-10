package com.neterium.client.sdk.configuration;

import com.neterium.client.sdk.properties.SdkProperties;
import com.neterium.client.sdk.throttling.DynamicThreadPoolManager;
import com.neterium.client.sdk.throttling.Throttleable;
import com.neterium.client.sdk.throttling.Throttler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Configuration of thread pooling.
 *
 * @author Bernard Ligny
 */
@Configuration(proxyBeanMethods = false)
@Import({
        DynamicThreadPoolManager.class,
})
public class ThreadingConfig {

    /**
     * Constructor
     */
    public ThreadingConfig() {
    }

    /**
     * Expose a pre-configured <code>ThreadPoolTaskExecutor</code> instance,
     * and qualify it as the one to use by the {@link Throttler}.
     *
     * @param sdkProperties SDK configuration
     * @return a <code>ThreadPoolTaskExecutor</code> instance
     */
    @Bean
    @Throttleable
    public ThreadPoolTaskExecutor pooledTaskExecutor(SdkProperties sdkProperties) {
        var config = sdkProperties.getThrottling().getCalibration();
        var taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(config.getInitialValue());
        taskExecutor.setMaxPoolSize(config.getMaxValue());
        taskExecutor.setThreadNamePrefix("Neterium-T");
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        taskExecutor.setVirtualThreads(true);
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        return taskExecutor;
    }

}
