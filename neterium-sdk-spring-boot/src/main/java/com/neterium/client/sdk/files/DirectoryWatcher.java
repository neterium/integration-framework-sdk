package com.neterium.client.sdk.files;

import com.neterium.client.sdk.exception.SdkException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.IntervalTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Base class for a component which is
 * <ul>
 *  <li>periodically watching the content of a directory</li>
 *  <li>delegating the processing of each found file to the configured {@link FileProcessor}</li>
 * </ul>
 *
 * @author Bernard Ligny
 */
@Slf4j
public class DirectoryWatcher implements SchedulingConfigurer {

    private final Path directory;
    private final FileProcessor fileProcessor;
    private final Duration fixedDelay;
    private final  Predicate<Path> fileFilter;


    /**
     * Constructor
     *
     * @param directory     directory to watch
     * @param fileFilter    predicate to filter files to consider
     * @param fixedDelay    delay between 2 runs
     * @param fileProcessor a {@link FileProcessor} instance
     */
    public DirectoryWatcher(Path directory,
                            Predicate<Path> fileFilter,
                            Duration fixedDelay,
                            FileProcessor fileProcessor) {
        this.directory = directory;
        this.fileFilter = fileFilter;
        this.fixedDelay = fixedDelay;
        this.fileProcessor = fileProcessor;
    }


    /**
     * @see SchedulingConfigurer#configureTasks(ScheduledTaskRegistrar)
     */
    @Override
    @SneakyThrows
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        registrar.addFixedDelayTask(
                new IntervalTask(this::scanDirectory, fixedDelay)
        );
    }

    
    /**
     * Scan the directory, and delegate the file processing to the configured {@link FileProcessor}
     */
    public void scanDirectory() {
        log.info("Scanning directory {}", directory.toAbsolutePath());
        try {
            Files.walkFileTree(directory, EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    if (fileFilter.test(file)) {
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
        } catch (IOException e) {
            log.error("Error while scanning directory", e);
            throw new SdkException(e);
        }
    }


    /**
     * Build a predicate to retain files with specific extensions
     * @param extensions array of extensions (ex: ".xml")
     * @return a <code>Predicate</code>
     */
    public static Predicate<Path> matchExtensions(String...extensions) {
        return path ->
                Stream.of(extensions)
                        .anyMatch( ext -> matchExtension(path, ext.toLowerCase()));
    }


    private static boolean matchExtension(Path path, String extension) {
        return path.getFileName()
                .toString()
                .toLowerCase()
                .endsWith(extension);
    }


}
