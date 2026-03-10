package com.neterium.client.sdk.files;

import com.neterium.client.sdk.exception.SdkException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

/**
 * Various services around flat files,
 * where exceptions are wrapped into {@link SdkException}
 *
 * @author Bernard Ligny
 */
@Service
public class FileService {

    /**
     * Constructor
     */
    public FileService() {
    }


    /**
     * Count the number of lines in a file
     *
     * @param path path to file
     * @return number of lines
     */
    public int countLines(Path path) {
        try (Stream<String> stream = Files.lines(path)) {
            return Math.toIntExact(stream.count());
        } catch (IOException e) {
            throw new SdkException("Error while counting lines of file %s".formatted(path), e);
        }
    }


    /**
     * Move a file
     *
     * @param sourceFile path of source file
     * @param targetFile path of target file
     */
    public void moveFile(Path sourceFile, Path targetFile) {
        try {
            Files.move(sourceFile, targetFile, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new SdkException("Error while moving file %s to %s".formatted(sourceFile, targetFile), e);
        }
    }


    /**
     * Save an {@link InputStream} to a file
     *
     * @param inputStream input stream to source data
     * @param targetFile  path of target file
     */
    public void saveToFile(InputStream inputStream, Path targetFile) {
        try {
            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new SdkException("saveToFile error", e);
        }
    }

}
