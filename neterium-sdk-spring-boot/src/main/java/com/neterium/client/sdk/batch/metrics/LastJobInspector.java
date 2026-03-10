package com.neterium.client.sdk.batch.metrics;

import com.neterium.client.sdk.instrumentation.Measurable;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;

/**
 * Component that is exposing some measurements about the last executed SpringBatch job
 *
 * @author Bernard Ligny
 */
@Component
public class LastJobInspector implements Measurable {

    private final JobRepository jobRepository;

    /**
     * Constructor
     *
     * @param jobRepository a <code>JobRepository</code> instance
     */
    public LastJobInspector(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }


    /**
     * @see Measurable#describe()
     */
    @Override
    public Map<String, Object> describe() {
        var results = this.getExecutionResults();
        return Map.of(
                "job-progress", computeProgress(results),
                "job-duration", computeDuration(results),
                "job-read-count", sum(results, ExecutionInfo::isWorkerStep, ExecutionInfo::read),
                "job-write-count", sum(results, ExecutionInfo::isWorkerStep, ExecutionInfo::write)
        );
    }


    /**
     * @see Measurable#getCategoryName()
     */
    @Override
    public String getCategoryName() {
        return "springbatch";
    }


    /**
     * Fetch and format execution results
     *
     * @return a list of {@link ExecutionInfo} instances
     */
    public List<ExecutionInfo> getExecutionResults() {
        var lastInstance = lastJobInstance();
        if (lastInstance.isPresent()) {
            return jobRepository.findJobExecutions(lastInstance.get())
                    .stream()
                    .findFirst()
                    .map(JobExecutionWrapper::new)
                    .map(JobExecutionWrapper::collectExecutionInfo)
                    .orElse(Collections.emptyList());
        }
        return Collections.emptyList();
    }


    /*
     * Query SpringBatch repository to get last run job
     */
    private Optional<JobInstance> lastJobInstance() {
        return jobRepository.getJobNames()
                .stream()
                .map(jobName -> jobRepository.findJobInstancesByName(jobName, 0, 1))
                .filter(c -> !c.isEmpty())
                .map(List::getFirst)
                .max(Comparator.comparingLong(JobInstance::getId));
    }


    /*
     * Compute overall progress by examining step executions
     */
    private long computeProgress(List<ExecutionInfo> entries) {
        if (entries.size() > 1) {
            long nbFinished = entries.stream()
                    .filter(ExecutionInfo::isStep)
                    .filter(next ->
                            Set.of(BatchStatus.COMPLETED.name(), BatchStatus.FAILED.name())
                                    .contains(next.status())
                    )
                    .count();
            var ratio = (double) nbFinished / (entries.size() - 1);
            return Double.valueOf(ratio * 100).longValue();
        } else {
            return 0L;
        }
    }


    /*
     * Compute job elapsed time
     */
    private long computeDuration(List<ExecutionInfo> entries) {
        var job = entries.stream()
                .filter(ExecutionInfo::isJob)
                .findFirst()
                .orElse(null);
        if (job != null) {
            var until = Optional.ofNullable(job.end())
                    .orElse(LocalDateTime.now());
            return Math.max(Duration.between(job.start(), until)
                    .toSeconds(), 1);
        } else {
            return 0;
        }
    }


    /*
     * Sum all entries matching the provided predicate
     */
    private long sum(List<ExecutionInfo> entries,
                     Predicate<ExecutionInfo> predicate,
                     ToLongFunction<ExecutionInfo> function) {
        ToLongFunction<ExecutionInfo> toLong = (e -> Optional.of(function.applyAsLong(e))
                .orElse(0L));
        return entries.stream()
                .filter(predicate)
                .mapToLong(toLong)
                .sum();
    }

}
