package com.neterium.client.sdk.screening;

import com.neterium.client.sdk.exception.SdkException;
import com.neterium.client.sdk.properties.SdkProperties;
import com.neterium.sdk.api.ExceptionsApi;
import com.neterium.sdk.api.RepositoryApi;
import com.neterium.sdk.model.CoreExceptionBody;
import com.neterium.sdk.model.CoreExceptionRequest;
import com.neterium.sdk.model.ProfilePublication;
import lombok.extern.slf4j.Slf4j;
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
        ProfilePublication publication = null;
        if (expireOnChecksumChange) {
            var foundProfiles = repositoryApi.getProfile(profileId).getData();
            if (foundProfiles.isEmpty()) {
                log.warn("Profile {} not found", profileId);
                throw new SdkException("Profile " + profileId + "not found ?!");
            }
            publication = foundProfiles.getFirst().getPublication();
        }
        var item = exceptionItem(screenedText, matchedText, profileId, expireOnChecksumChange, publication);
        var body = new CoreExceptionBody()
                .addExceptionsItem(item);
        var outcome = exceptionsApi.createExceptions(body);
        if (outcome.getCount().intValue() > 0) {
            var id = outcome.getData().getFirst().getId();
            log.info("Exception successfully created with ID {}", id);
            return id;
        } else {
            log.error("");
            throw new SdkException("Exception creation failed");
        }
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


    private CoreExceptionRequest exceptionItem(String screenedText,
                                               String matchedText,
                                               String profileId,
                                               boolean expire,
                                               ProfilePublication publication) {
        var item = new CoreExceptionRequest()
                .reference(UUID.randomUUID().toString())
                .clientReference(sdkProperties.getScreening().getClientReference())
                .source(screenedText)
                .match(matchedText)
                .profileId(profileId);
        if (expire) {
            if (sdkProperties.getExceptions().isUseCoreCheckSum()) {
                item.setExpirationType(CoreExceptionRequest.ExpirationTypeEnum.CORE_PROFILE);
                item.setCoreChecksum(BigDecimal.valueOf(publication.getCoreChecksum()));
            } else {
                item.setExpirationType(CoreExceptionRequest.ExpirationTypeEnum.PROFILE);
                item.setProfileChecksum(BigDecimal.valueOf(publication.getChecksum()));
            }
        } else {
            item.setExpirationType(CoreExceptionRequest.ExpirationTypeEnum.NEVER);
        }
        return item;
    }

}
