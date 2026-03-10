package com.neterium.client.sdk.files;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * A component which is
 * <ul>
 *  <li>periodically watching the content of a directory</li>
 *  <li>delegating the processing of each found file to the configured {@link FileProcessor}</li>
 * </ul>
 *
 * @author Bernard Ligny
 */
@Component
@ConditionalOnProperty("neterium.polling.interval")
@Slf4j
public class DirectoryWatcher {

    private final Path directory;
    private final FileProcessor fileProcessor;


    /**
     * Constructor
     *
     * @param directory     directory to watch
     * @param fileProcessor a {@link FileProcessor} instance
     */
    public DirectoryWatcher(@Value("${neterium.polling.directory}") Path directory,
                            FileProcessor fileProcessor) {
        this.directory = directory;
        this.fileProcessor = fileProcessor;
    }

    private static Predicate<Path> matchExtension(String extension) {
        return path -> path.getFileName()
                .toString()
                .toLowerCase()
                .endsWith(extension);
    }


    /**
     * Scan the directory, and delegate the file processing to the configured {@link FileProcessor}
     *
     * @throws IOException in case of I/O exception
     */
    @Scheduled(fixedDelayString = "${neterium.polling.interval}", initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void scanDirectory() throws IOException {
        log.info("Scanning directory {}", directory.toAbsolutePath());
        var filter = matchExtension(".csv");
        Files.walkFileTree(directory, EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                if (filter.test(file)) {
                    log.debug("... File detected: {}", file);
                    fileProcessor.process(file);
                    return FileVisitResult.CONTINUE;
                } else {
                    return FileVisitResult.SKIP_SUBTREE;
                }
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exception) {
                log.warn("Unable to process file %s".formatted(file), exception);
                return FileVisitResult.CONTINUE;
            }
        });

    }

}
