package com.neterium.client.sdk.configuration;

import com.neterium.client.sdk.batch.listeners.SessionJobListener;
import com.neterium.client.sdk.batch.listeners.StepExecutionLogger;
import com.neterium.client.sdk.batch.metrics.LastJobInspector;
import com.neterium.client.sdk.batch.support.NeteriumBuilder;
import com.neterium.client.sdk.exception.FatalException;
import com.neterium.client.sdk.exception.RetryableException;
import com.neterium.client.sdk.properties.SdkProperties;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.util.Map;

/**
 * Configuration of SpringBatch jobs
 *
 * @author Bernard Ligny
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({Job.class, JobRepository.class})
@Import({
        LastJobInspector.class,
        SessionJobListener.class,
        StepExecutionLogger.class,
        NeteriumBuilder.class
})
public class BatchConfig {

    /**
     * Constructor
     */
    public BatchConfig() {
    }

    
    /**
     * Expose a pre-configured <code>RetryPolicy</code> instance
     *
     * @param sdkProperties SDK configuration
     * @return a <code>RetryPolicy</code> instance
     */
    @Bean
    public RetryPolicy retryPolicy(SdkProperties sdkProperties) {
        final Map<Class<? extends Throwable>, Boolean> retryableExceptions = Map.of(
                RetryableException.class, true,
                FatalException.class, false
        );
        var maxRetries = sdkProperties.getJobs().getMaxRetries();
        return new SimpleRetryPolicy(maxRetries, retryableExceptions, true, false);
    }


    /**
     * Expose a pre-configured <strong>asynchronous</strong> {@link JobLauncher} instance
     *
     * @param jobRepository a <code>JobRepository</code> instance
     * @return a <code>JobLauncher</code> instance
     */
    @Bean
    @Qualifier("async")
    public JobLauncher asyncJobLauncher(JobRepository jobRepository) {
        var taskExecutorJobLauncher = new TaskExecutorJobLauncher();
        taskExecutorJobLauncher.setJobRepository(jobRepository);
        var executor = new SimpleAsyncTaskExecutor("job-launcher-");
        taskExecutorJobLauncher.setTaskExecutor(executor);
        executor.setVirtualThreads(true);
        try {
            taskExecutorJobLauncher.afterPropertiesSet();
            return taskExecutorJobLauncher;
        } catch (Exception e) {
            throw new BatchConfigurationException("Unable to configure JobLauncher", e);
        }
    }

}
