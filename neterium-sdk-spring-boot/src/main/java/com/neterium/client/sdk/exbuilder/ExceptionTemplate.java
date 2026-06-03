package com.neterium.client.sdk.exbuilder;

import com.neterium.client.sdk.exception.SdkException;
import com.neterium.client.sdk.properties.SdkProperties;
import com.neterium.sdk.api.ExceptionsApi;
import com.neterium.sdk.api.RepositoryApi;
import com.neterium.sdk.model.CoreExceptionBody;
import com.neterium.sdk.model.CoreExceptionRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * This class simplifies exception management,
 * by eliminating boilerplate code that is needed to invoke Neterium exceptions API.
 *
 * @author Bernard Ligny
 */
@Component
@Slf4j
public class ExceptionTemplate {

    private final SdkProperties sdkProperties;
    private final ExceptionsApi exceptionsApi;
    private final RepositoryApi repositoryApi;


    /**
     * Constructor
     *
     * @param sdkProperties SDK configuration bean
     * @param exceptionsApi a {@link ExceptionsApi} instance
     * @param repositoryApi a {@link RepositoryApi} instance
     */
    public ExceptionTemplate(SdkProperties sdkProperties,
                             ExceptionsApi exceptionsApi,
                             RepositoryApi repositoryApi) {
        this.sdkProperties = sdkProperties;
        this.exceptionsApi = exceptionsApi;
        this.repositoryApi = repositoryApi;
    }


    /**
     * Create a white-list exception to get rid of a noisy match
     *
     * @param matchedText            the match to avoid
     * @param profileId              id of matched profile
     * @param expireOnChecksumChange whether to expire exception on profile update
     * @return id of created exception
     */
    public String createWhiteListException(String matchedText,
                                           String profileId,
                                           boolean expireOnChecksumChange) {
        return this.createWhiteListException(null, matchedText, profileId, expireOnChecksumChange);
    }


    /**
     * Create a white-list exception to get rid of a noisy match
     *
     * @param screenedText           the text triggering the match
     * @param matchedText            the match to avoid
     * @param profileId              id of matched profile
     * @param expireOnChecksumChange whether to expire exception on profile update
     * @return id of created exception
     */
    public String createWhiteListException(String screenedText,
                                           String matchedText,
                                           String profileId,
                                           boolean expireOnChecksumChange) {
        var request = new CoreExceptionRequest()
                .source(screenedText)
                .match(matchedText);
        var ref = UUID.randomUUID().toString();
        populateRequest(request, ref, profileId, expireOnChecksumChange);
        return doCreate(request);
    }


    public CoreExceptionRequest previewCustomException(String reference,
                                                       String expressionArray,
                                                       String profileId,
                                                       boolean expireOnChecksumChange) {
        var request = new CoreExceptionRequest();
        var jsonArray = new JSONArray(expressionArray);
        for (int i = 0; i < jsonArray.length(); i++) {
            request.getConditions().add(jsonArray.get(i));
        }
        populateRequest(request, reference, profileId, expireOnChecksumChange);
        return request;
    }

    public String createCustomException(String reference,
                                        String rawExpression,
                                        String profileId,
                                        boolean expireOnChecksumChange) {
        var request = previewCustomException(reference, rawExpression, profileId, expireOnChecksumChange);
        return doCreate(request);
    }


    /**
     * Delete an exception
     *
     * @param exceptionId id of exception to delete
     * @return true if deletion is successful, false otherwise
     */
    public boolean deleteException(String exceptionId) {
        var resp = exceptionsApi.deleteExceptionWithHttpInfo(exceptionId, true);
        if (resp.getStatusCode().is2xxSuccessful()) {
            log.info("Exception {} successfully deleted", exceptionId);
            return true;
        } else {
            log.warn("Exception {} was NOT deleted", exceptionId);
            return false;
        }
    }


    private void populateRequest(CoreExceptionRequest request, String reference,
                                 String profileId, boolean expire) {
        request.reference(reference)
                .clientReference(sdkProperties.getScreening().getClientReference())
                .profileId(profileId);
        if (expire && StringUtils.isNotEmpty(profileId)) {

            var foundProfiles = repositoryApi.getProfile(profileId).getData();
            if (foundProfiles.isEmpty()) {
                log.warn("Profile {} not found", profileId);
                throw new SdkException("Profile " + profileId + "not found ?!");
            }
            var publication = foundProfiles.getFirst().getPublication();
            if (sdkProperties.getExceptions().isUseCoreCheckSum()) {
                request.setExpirationType(CoreExceptionRequest.ExpirationTypeEnum.CORE_PROFILE);
                request.setCoreChecksum(BigDecimal.valueOf(publication.getCoreChecksum()));
            } else {
                request.setExpirationType(CoreExceptionRequest.ExpirationTypeEnum.PROFILE);
                request.setProfileChecksum(BigDecimal.valueOf(publication.getChecksum()));
            }
        } else {
            request.setExpirationType(CoreExceptionRequest.ExpirationTypeEnum.NEVER);
        }
    }


    private String doCreate(CoreExceptionRequest request) {
        var body = new CoreExceptionBody()
                .addExceptionsItem(request);
        var outcome = exceptionsApi.createExceptions(body);
        if (outcome.getCount().intValue() > 0) {
            var id = outcome.getData().getFirst().getId();
            log.info("Exception successfully created with ID {}", id);
            return id;
        } else {
            log.error("Unsuccessful outcome: {}", outcome.getOutcome());
            throw new SdkException("Exception creation failed");
        }
    }

}
