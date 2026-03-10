package com.neterium.client.sdk.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.neterium.client.sdk.utils.FileUtils;

/**
 * A helper to split a file into a collection of N individual files.
 * Being stateful, FileSplitter instances should be disposed when stream of split results
 * is fully consumed.
 *
 * @author Bernard Ligny
 */
@Slf4j
public class FileSplitter {

    private final String delimiter;
    private final String extension;

    private Path outputDir = null;


    /**
     * Create a new file splitter with default extension (.tmp) for child files
     * @param delimiter delimiter to use as fragment separator
     */
    public FileSplitter(String delimiter) {
        this(delimiter, ".tmp");
    }


    /**
     * Create a new file splitter with default extension (.tmp) for child files
     * @param delimiter delimiter to use as fragment separator
     * @param ext desired extension for temp files
     */
    public FileSplitter(String delimiter, String ext) {
        this.delimiter = delimiter;
        this.extension = ext;
    }


    /**
     * Split a source file
     * @param inputFile the file to split
     * @return a stream of child files
     * @throws IOException in case of I/O exception
     */
    public Stream<Path> split(Path inputFile) throws IOException {
        dispose();
        outputDir = Files.createTempDirectory("fsplit-");
        outputDir.toFile().deleteOnExit();
        var outputFiles = new ArrayList<Path>();
        var part = new AtomicInteger(0);
        var writer = newChild(part, outputFiles);
        try (var lines = Files.lines(inputFile)) {
            for (String line : (Iterable<String>) lines::iterator) {
                writer.write(line);
                writer.newLine();
                if (line.trim().equals(delimiter)) {
                    writer.close();
                    writer = newChild(part, outputFiles);
                }
            }
            outputFiles.removeLast();
            log.info("'{}' split into {} files in {}", inputFile.getFileName(), outputFiles.size(), outputDir);
            return outputFiles.stream();
        } finally {
            writer.close();
        }
    }


    /**
     * Dispose this instance, cleaning working tmp files
     */
    public void dispose() {
        if (outputDir != null) {
            try (var stream = Files.list(outputDir)) {
                stream.forEach(path -> {
                    log.trace("Deleting tmp file {}", path);
                    FileUtils.silentDelete(path);
                });
                FileUtils.silentDelete(outputDir);
            } catch (IOException ignored) {
            }
        }
    }


    private BufferedWriter newChild(AtomicInteger part,
                                    List<Path> outputFiles) throws IOException {
        var child = outputDir.resolve("part-" + part.incrementAndGet() + extension);
        outputFiles.add(child);
        log.trace("New tmp file: {}", child.toAbsolutePath());
        return Files.newBufferedWriter(child);
    }


}
