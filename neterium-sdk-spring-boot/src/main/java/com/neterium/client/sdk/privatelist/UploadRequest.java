package com.neterium.client.sdk.privatelist;

import lombok.Builder;
import lombok.Getter;

import java.io.File;
import java.util.Optional;

/**
 * Encapsulation of all parameters needed by an upload operation
 *
 * @author Bernard Ligny
 */
@Builder(setterPrefix = "with")
@Getter
public class UploadRequest {

    private String listId;
    private String clientReference;
    private boolean classify;
    private boolean transliterate;
    private File file;
    private boolean deltaMode = false;
    private Action action;

    /**
     * Possible actions on provided records of file
     */
    enum Action {
        ADD,
        UPDATE,
        DELETE
    }


    /**
     * Get the specified list identifier
     *
     * @return optional list id
     */
    public Optional<String> getListId() {
        return Optional.ofNullable(listId);
    }
}
