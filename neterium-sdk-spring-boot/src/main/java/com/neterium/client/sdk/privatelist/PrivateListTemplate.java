package com.neterium.client.sdk.privatelist;

import com.neterium.client.sdk.exception.SdkException;
import com.neterium.sdk.api.ListsApi;
import com.neterium.sdk.api.RepositoryApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Optional;

/**
 * This class simplifies the management of private lists
 * by eliminating boilerplate code that is needed to invoke Neterium list API.
 *
 * @author Bernard Ligny
 */
@Component
@Slf4j
public class PrivateListTemplate {

    private final ListsApi listApi;


    /**
     * Constructor
     *
     * @param listApi a {@link RepositoryApi} instance
     */
    public PrivateListTemplate(ListsApi listApi) {
        this.listApi = listApi;
    }


    /**
     * Upload a (new or existing) private list
     *
     * @param request upload parameters
     * @return id of created or updated private list
     */
    public String uploadPrivateList(UploadRequest request) {
        var checkSum = computeCheckSum(request.getFile());
        String listId;
        if (request.isDeltaMode()) {
            listId = request.getListId()
                    .orElseThrow(() -> new SdkException("ListID is mandatory in DELTA mode"));
            var action = Optional.ofNullable(request.getAction())
                    .map(v -> v.toString().toLowerCase())
                    .orElse(null);
            listApi.uploadDeltaList(listId,
                    request.isClassify(),
                    request.isTransliterate(),
                    true,
                    request.getClientReference(),
                    checkSum,
                    action,
                    request.getFile()
            );
            log.debug("List {} successfully updated", listId);
        } else {
            listId = request.getListId()
                    .orElseGet(this::generateListId);
            var outcome = listApi.uploadList(listId,
                    request.isClassify(),
                    request.isTransliterate(),
                    true,
                    request.getClientReference(),
                    checkSum,
                    request.getFile()
            );
            log.debug("List {} successfully created ({} records)", request.getListId(), outcome.getCount());
        }
        return listId;
    }


    /**
     * Delete a private list
     *
     * @param listId    id of the list to delete
     * @param clientRef client reference
     */
    public void deletePrivateList(String listId, String clientRef) {
        listApi.deleteList(listId, clientRef);
    }


    private String computeCheckSum(File file) {
        try (var is = new FileInputStream(file)) {
            return DigestUtils.md5DigestAsHex(is);
        } catch (IOException e) {
            log.warn("Error while computing MD5 checksum", e);
            return null;
        }
    }

    private String generateListId() {
        return RandomStringUtils.secureStrong().nextAlphabetic(4);
    }

}
