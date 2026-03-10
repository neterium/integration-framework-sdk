package com.neterium.client.sdk.session;

import com.neterium.sdk.api.SessionApi;
import com.neterium.sdk.model.CoreSession;
import com.neterium.sdk.model.CoreSessionRequest;
import com.neterium.sdk.model.CoreSessionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * A facade around the {@link SessionApi} proxy to ease the management of sessions
 * during a screening process
 *
 * @author Bernard Ligny
 */
@Component
@Slf4j
public class SessionManager {


    private final SessionApi sessionApi;

    /**
     * Constructor
     *
     * @param sessionApi a {@link SessionApi} instance
     */
    public SessionManager(SessionApi sessionApi) {
        this.sessionApi = sessionApi;
    }


    /**
     * Start a new session
     *
     * @return ID of create session
     */
    public String createSession() {
        return doInvoke(sessionApi::startSession);
    }


    /**
     * Start a new session with some reference data
     *
     * @param reference        Your own session reference
     * @param clientReference  Your client ID if you need one
     * @param projectReference Your project ID if you need one
     * @return ID of create session
     */
    public String createSession(String reference, String clientReference, String projectReference) {
        var request = new CoreSessionRequest()
                .reference(reference)
                .clientReference(clientReference)
                .projectReference(projectReference);
        return doInvoke(() -> sessionApi.createSession(request));
    }


    /**
     * Close a session based on its id
     *
     * @param sessionId ID of the session to close
     */
    public void closeSession(String sessionId) {
        try {
            sessionApi.closeSession(sessionId);
            log.info("Session {} closed", sessionId);
        } catch (Throwable t) {
            log.warn("Error while closing session", t);
        }
    }


    /**
     * Invoke session API
     */
    private String doInvoke(Supplier<CoreSessionResponse> responseProvider) {
        try {
            var response = responseProvider.get();
            var sessionId = response.getData()
                    .stream()
                    .findFirst()
                    .map(CoreSession::getId)
                    .orElse(null);
            log.info("New session created with id: {}", sessionId);
            return sessionId;
        } catch (Throwable t) {
            log.warn("Unable to create session", t);
            return null;
        }
    }


}
