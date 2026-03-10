package com.neterium.client.sdk.batch.metrics;

import org.springframework.batch.core.JobExecution;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around SpringBatch <code>JobExecution</code> to ease extraction of execution results
 *
 * @author Bernard Ligny
 */
public class JobExecutionWrapper {

    private final JobExecution jobExecution;

    /**
     * Constructor
     *
     * @param jobExecution the <code>JobExecution</code> to wrap
     */
    public JobExecutionWrapper(JobExecution jobExecution) {
        this.jobExecution = jobExecution;
    }


    /**
     * Extract detailed information about the job execution
     * <ul>
     *  <li>for the whole job</li>
     *  <li>for each step</li>
     * </ul>
     *
     * @return a list of {@link ExecutionInfo}
     */
    public List<ExecutionInfo> collectExecutionInfo() {
        var items = new ArrayList<ExecutionInfo>();
        items.add(
                new ExecutionInfo("Job",
                        jobExecution.getJobInstance().getJobName(),
                        jobExecution.getStartTime(),
                        jobExecution.getEndTime(),
                        jobExecution.getStatus().name(),
                        null,
                        null,
                        null)
        );
        jobExecution.getStepExecutions()
                .forEach(stepExecution -> items.add(
                        new ExecutionInfo("Step",
                                stepExecution.getStepName(),
                                stepExecution.getStartTime(),
                                stepExecution.getEndTime(),
                                stepExecution.getStatus().name(),
                                stepExecution.getReadCount(),
                                stepExecution.getWriteCount(),
                                stepExecution.getSkipCount()
                        )));
        return items;
    }


}
