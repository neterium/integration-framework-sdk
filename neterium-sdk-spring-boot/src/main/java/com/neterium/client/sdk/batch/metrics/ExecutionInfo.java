package com.neterium.client.sdk.batch.metrics;

import org.springframework.format.annotation.DurationFormat;
import org.springframework.format.datetime.standard.DurationFormatterUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * A structure holding execution information about a SpringBatch process
 * (job, step, or worker step)
 *
 * @param type    process type (e.g. job, step, worker, ...cl)
 * @param name    process name
 * @param start   start time
 * @param end     end time
 * @param status  execution status
 * @param read    number of read items
 * @param write   number of written items
 * @param skipped number of skipped items
 * @author Bernard Ligny
 */
public record ExecutionInfo(String type,
                            String name,
                            LocalDateTime start,
                            LocalDateTime end,
                            String status,
                            Long read,
                            Long write,
                            Long skipped) {

    /**
     * Compute the process duration
     *
     * @return a duration string with appropriate unit
     */
    public String getDuration() {
        if (start != null && end != null) {
            var duration = Duration.between(start, end);
            if (duration.toSeconds() > 1) {
                duration = duration.truncatedTo(ChronoUnit.SECONDS);
            } else {
                duration = duration.truncatedTo(ChronoUnit.MILLIS);
            }
            return DurationFormatterUtils.print(duration, DurationFormat.Style.COMPOSITE);
        } else {
            return "";
        }
    }

    /**
     * Determine whether the execution info relates to a job
     *
     * @return true if relates to a job, false otherwise
     */
    public boolean isJob() {
        return "Job".equalsIgnoreCase(type);
    }

    /**
     * Determine whether the execution info relates to a regular step
     *
     * @return true if relates to a regular step, false otherwise
     */
    public boolean isStep() {
        return "Step".equalsIgnoreCase(type);
    }

    /**
     * Determine whether the execution info relates to a worker step
     *
     * @return true if relates to a worker step, false otherwise
     */
    public boolean isWorkerStep() {
        return isStep() && name.toLowerCase().contains("worker");
    }

}
