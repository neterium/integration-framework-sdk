package com.neterium.client.sdk.files;

import java.nio.file.Path;

/**
 * Abstraction of the processing of a file
 *
 * @author Bernard Ligny
 */
@FunctionalInterface
public interface FileProcessor {

    /**
     * Process a file
     *
     * @param path path of the file to process
     */
    void process(Path path);
}