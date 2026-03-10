package com.neterium.client.sdk.batch.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * A <code>StepExecutionListener</code> implementation used to log step results.
 *
 * @author Bernard Ligny
 */
@Component
@Slf4j
public class StepExecutionLogger implements StepExecutionListener {

    /**
     * Constructor
     */
    public StepExecutionLogger() {
    }

    
    /**
     * 'afterStep' hook
     *
     * @param stepExecution a <code>StepExecution</code> instance
     * @return a null <code>ExitStatus</code> to leave the old value unchanged.
     */
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (stepExecution.getStatus().isUnsuccessful()) {
            var firstException = stepExecution.getFailureExceptions()
                    .stream()
                    .findFirst()
                    .orElse(null);
            log.error("Failed execution of '{}'", stepExecution.getStepName(), firstException);
            logExecutionStatus(false, stepExecution);
        } else {
            logExecutionStatus(true, stepExecution);
        }
        return null;
    }


    private void logExecutionStatus(boolean positive, StepExecution stepExecution) {
        log.info("{} execution of '{}' - Read={} | Write={} | Skip={} | Commit={} | Rollback={}",
                (positive ? "Successful" : "Failed"),
                stepExecution.getStepName(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getSkipCount(),
                stepExecution.getCommitCount(),
                stepExecution.getRollbackCount());
    }

}
