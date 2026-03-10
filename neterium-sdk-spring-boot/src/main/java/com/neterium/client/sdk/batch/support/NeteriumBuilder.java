package com.neterium.client.sdk.batch.support;

import com.neterium.client.sdk.batch.listeners.SessionJobListener;
import com.neterium.client.sdk.batch.listeners.StepExecutionLogger;
import com.neterium.client.sdk.exception.SkippableException;
import com.neterium.client.sdk.throttling.Throttleable;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
import org.springframework.batch.core.step.builder.PartitionStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * A utility component to ease the creation of pre-configured SpringBatch <code>Job</code>
 * and <code>Step</code> instances that can benefit from SDK features such as throttling,
 * instrumentation, etc...
 *
 * @author Bernard Ligny
 */
@Component
public class NeteriumBuilder {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TaskExecutor pooledTaskExecutor;
    private final SessionJobListener sessionJobListener;
    private final StepExecutionLogger executionListener;
    private final RetryPolicy retryPolicy;


    /**
     * Constructor
     *
     * @param jobRepository      a <code>JobRepository</code> instance
     * @param transactionManager a <code>PlatformTransactionManager</code> instance
     * @param pooledTaskExecutor a {@link Throttleable} <code>TaskExecutor</code> instance
     * @param sessionJobListener a {@link SessionJobListener} instance
     * @param executionListener  a {@link  StepExecutionLogger} instance
     * @param retryPolicy        a <code>RetryPolicy</code> instance
     */
    public NeteriumBuilder(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           @Throttleable TaskExecutor pooledTaskExecutor,
                           SessionJobListener sessionJobListener,
                           StepExecutionLogger executionListener,
                           RetryPolicy retryPolicy) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.pooledTaskExecutor = pooledTaskExecutor;
        this.sessionJobListener = sessionJobListener;
        this.executionListener = executionListener;
        this.retryPolicy = retryPolicy;
    }


    /**
     * Get SpringBatch default <code>JobBuilder</code>
     *
     * @param jobName name of the job to build
     * @return a <code>JobBuilder</code> instance
     */
    public JobBuilder jobBuilder(String jobName) {
        return new JobBuilder(jobName, jobRepository);
    }


    /**
     * Enhanced version of SpringBatch <code>JobBuilder</code>
     * with a pre-configured {@link SessionJobListener}
     *
     * @param jobName name of the job to build
     * @return a <code>JobBuilder</code> instance
     */
    public JobBuilder sessionAwareJobBuilder(String jobName) {
        return jobBuilder(jobName)
                .listener(sessionJobListener);
    }


    /**
     * Enhanced version of SpringBatch <code>PartitionStepBuilder</code>
     * with a pre-configured throttleable taskExecutor
     *
     * @param stepName    name of the step to build
     * @param gridSize    the grid size
     * @param partitioner a <code>Partitioner</code> instance
     * @param workerStep  a worker <code>Step</code> instance
     * @return a <code>PartitionStepBuilder</code> instance
     */
    public PartitionStepBuilder partitionedStepBuilder(String stepName,
                                                       int gridSize,
                                                       Partitioner partitioner,
                                                       Step workerStep) {
        return new StepBuilder(stepName, jobRepository)
                .partitioner(workerStep.getName(), partitioner)
                .step(workerStep)
                .gridSize(gridSize)
                .taskExecutor(pooledTaskExecutor)
                .listener(executionListener);
    }


    /**
     * Enhanced version of SpringBatch <code>FaultTolerantStepBuilder</code>
     * with the standard completion policy
     *
     * @param stepName  name of the step to build
     * @param chunkSize the chunk size
     * @param reader    a <code>ItemReader</code> instance
     * @param writer    a <code>ItemWriter</code> instance
     * @param <I>       the type of item to be processed as input
     * @param <O>       the type of item to be output
     * @return a <code>FaultTolerantStepBuilder</code> instance
     */
    public <I, O> FaultTolerantStepBuilder<I, O> workerStepBuilder(String stepName,
                                                                   int chunkSize,
                                                                   ItemReader<I> reader,
                                                                   ItemWriter<O> writer) {
        var chunkCompletionPolicy = new SimpleCompletionPolicy(chunkSize);
        return workerStepBuilder(stepName, chunkCompletionPolicy, reader, writer);
    }


    /**
     * Enhanced version of SpringBatch <code>FaultTolerantStepBuilder</code>
     * with a custom completion policy
     *
     * @param stepName              name of the step to build
     * @param chunkCompletionPolicy a <code>CompletionPolicy</code> instance
     * @param reader                a <code>ItemReader</code> instance
     * @param writer                a <code>ItemWriter</code> instance
     * @param <I>                   the type of item to be processed as input
     * @param <O>                   the type of item to be output
     * @return a <code>FaultTolerantStepBuilder</code> instance
     */
    public <I, O> FaultTolerantStepBuilder<I, O> workerStepBuilder(String stepName,
                                                                   CompletionPolicy chunkCompletionPolicy,
                                                                   ItemReader<I> reader,
                                                                   ItemWriter<O> writer) {
        return new StepBuilder(stepName, jobRepository)
                .<I, O>chunk(chunkCompletionPolicy, transactionManager)
                .reader(reader)
                .writer(writer)
                .faultTolerant()
                .skip(SkippableException.class)
                .skipLimit(Integer.MAX_VALUE)
                .noRollback(SkippableException.class)
                .retryPolicy(retryPolicy)
                .backOffPolicy(new ExponentialBackOffPolicy());
    }


    /**
     * Enhanced version of SpringBatch <code>FaultTolerantStepBuilder</code>
     * with the standard completion policy
     *
     * @param stepName  name of the step to build
     * @param chunkSize the chunk size
     * @param reader    a <code>ItemReader</code> instance
     * @param processor a <code>ItemProcessor</code> instance
     * @param writer    a <code>ItemWriter</code> instance
     * @param <I>       the type of item to be processed as input
     * @param <O>       the type of item to be output
     * @return a <code>FaultTolerantStepBuilder</code> instance
     */
    public <I, O> FaultTolerantStepBuilder<I, O> workerStepBuilder(String stepName,
                                                                   int chunkSize,
                                                                   ItemReader<I> reader,
                                                                   ItemProcessor<I, O> processor,
                                                                   ItemWriter<O> writer) {
        var chunkCompletionPolicy = new SimpleCompletionPolicy(chunkSize);
        return workerStepBuilder(stepName, chunkCompletionPolicy, reader, processor, writer);
    }


    /**
     * Enhanced version of SpringBatch <code>FaultTolerantStepBuilder</code>
     * with a custom completion policy
     *
     * @param stepName              name of the step to build
     * @param chunkCompletionPolicy a <code>CompletionPolicy</code> instance
     * @param reader                a <code>ItemReader</code> instance
     * @param processor             a <code>ItemProcessor</code> instance
     * @param writer                a <code>ItemWriter</code> instance
     * @param <I>                   the type of item to be processed as input
     * @param <O>                   the type of item to be output
     * @return a <code>FaultTolerantStepBuilder</code> instance
     */
    public <I, O> FaultTolerantStepBuilder<I, O> workerStepBuilder(String stepName,
                                                                   CompletionPolicy chunkCompletionPolicy,
                                                                   ItemReader<I> reader,
                                                                   ItemProcessor<I, O> processor,
                                                                   ItemWriter<O> writer) {
        return new StepBuilder(stepName, jobRepository)
                .<I, O>chunk(chunkCompletionPolicy, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(SkippableException.class)
                .skipLimit(Integer.MAX_VALUE)
                .noRollback(SkippableException.class)
                .retryPolicy(retryPolicy)
                .backOffPolicy(new ExponentialBackOffPolicy());
    }
    
}
