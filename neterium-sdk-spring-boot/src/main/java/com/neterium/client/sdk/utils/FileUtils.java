package com.neterium.client.sdk.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

/**
 * FileUtils
 *
 * @author Bernard Ligny
 */
@Slf4j
public class FileUtils {

    /**
     * Disable instantiation
     */
    private FileUtils() {
    }

    /**
     * Silent deletion of a single file
     * @param path the file to delete
     */
    public static void silentDelete(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Unable to delete file", e);
        }
    }

    /**
     * Silent deletion of a collection of files
     * @param paths the files to delete
     */
    public static void silentDelete(Collection<Path> paths) {
        paths.forEach(FileUtils::silentDelete);
    }

}
