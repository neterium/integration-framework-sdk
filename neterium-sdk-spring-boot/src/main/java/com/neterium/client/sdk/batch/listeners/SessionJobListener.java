package com.neterium.client.sdk.batch.listeners;

import com.neterium.client.sdk.session.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

/**
 * A <code>JobExecutionListener</code> implementation that is
 * <ul>
 *  <li>creating a Neterium session when job is started</li>
 *  <li>putting session id in job execution context</li>
 *  <li>closing the session when job is terminated</li>
 * </ul>
 *
 * @author Bernard Ligny
 */
@Component
@Slf4j
public class SessionJobListener implements JobExecutionListener {

    /**
     * Name of the session entry in the job <code>ExecutionContext</code>
     */
    public static final String SESSION_ID = "SESSION_ID";

    private final SessionManager sessionManager;

    /**
     * Constructor
     *
     * @param sessionManager a {@link SessionManager} instance
     */
    public SessionJobListener(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }


    /**
     * Create a new session when job is started
     *
     * @param jobExecution the current <code>JobExecution</code>
     */
    @Override
    public void beforeJob(JobExecution jobExecution) {
        var sessionId = sessionManager.createSession(null, "Neterium", "JetScan-Demo");
        jobExecution.getExecutionContext().putString(SESSION_ID, sessionId);
    }


    /**
     * Close job-bounded session when job is terminated
     *
     * @param jobExecution the current <code>JobExecution</code>
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        String sessionId = jobExecution.getExecutionContext().get(SESSION_ID, String.class);
        if (sessionId != null) {
            sessionManager.closeSession(sessionId);
        }
    }


}
