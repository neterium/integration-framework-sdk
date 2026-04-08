package com.neterium.client.sdk.privatelist;

import lombok.Builder;
import lombok.Getter;

import java.io.File;
import java.util.Optional;

/**
 * UploadRequest
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

    enum Action {
        ADD,
        UPDATE,
        DELETE
    }

    public Optional<String> getListId() {
        return Optional.ofNullable(listId);
    }
}
