package com.neterium.client.sdk.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neterium.client.sdk.binding.JetFlowBinder;
import com.neterium.client.sdk.mapping.Format;
import com.neterium.client.sdk.utils.FileSplitter;
import com.neterium.client.sdk.utils.FileUtils;
import com.neterium.client.sdk.utils.JsonHelper;
import com.neterium.sdk.model.JetFlowRequestBody;
import com.prowidesoftware.swift.model.mt.AbstractMT;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base class for {@link JetFlowConverter} of SWIFT FIN files
 *
 * @author Bernard Ligny
 */
@Slf4j
public abstract class SwiftFinConverter extends JetFlowConverterSupport<SimpleTransaction> {

    private final Format format;

    /**
     * Constructor
     *
     * @param jacksonMapper a Jackson <code>ObjectMapper</code> instance
     * @param format        format of the input transactions
     * @param binder        a {@link JetFlowBinder} instance
     * @param jsonHelper    a {@link JsonHelper} instance
     * @param beanType      concrete type of transaction beans
     */
    public SwiftFinConverter(ObjectMapper jacksonMapper,
                             Format format,
                             JetFlowBinder binder,
                             JsonHelper jsonHelper,
                             Class<SimpleTransaction> beanType) {
        super(jacksonMapper, format, binder, jsonHelper, beanType);
        this.format = format;
    }


    /**
     * @see JetFlowConverter#handle(Format)
     */
    @Override
    public boolean handle(Format format) {
        return this.format.equals(format);
    }


    /**
     * @see JetFlowConverter#convert(Path, int)
     */
    @Override
    public Stream<JetFlowRequestBody> convert(Path inputFilePath, int batchSize) throws IOException {
        // First convert regular file to intermediate format
        var tmpJsonFilePath = finToProwide(inputFilePath);
        // Then use jetflow generic converter
        var stream = super.convert(tmpJsonFilePath, batchSize);
        return stream.onClose(() -> {
            System.out.println("**** onClose");
            try {
                Files.deleteIfExists(tmpJsonFilePath);
            } catch (IOException ignored) {
            }
        });
    }


    /**
     * @see JetFlowConverter#convertBatch(Path, String, int)
     */
    @Override
    public Stream<JetFlowRequestBody> convertBatch(Path inputFilePath, String delimiter, int batchSize) throws IOException {
        // First convert batch file to separate files in intermediate format
        var streamOfFiles = finBatchToProwide(inputFilePath, delimiter);
        // Then use jetflow generic converter
        return super.doConvert(streamOfFiles, batchSize)
                .onClose(streamOfFiles::close);
    }


    /**
     * @see JetFlowConverter#convertAndSerializeBatch(Path, String, int, boolean)
     */
    @Override
    public Stream<String> convertAndSerializeBatch(Path inputFilePath, String delimiter, int batchSize, boolean pretty) throws IOException {
        // First convert batch file to separate files intermediate format
        var streamOfFiles = finBatchToProwide(inputFilePath, delimiter);
        // Then use jetflow generic converter
        return super.doConvertAndSerialize(streamOfFiles, batchSize, pretty)
                .onClose(streamOfFiles::close);
    }


    private Path finToProwide(Path finFilePath) throws IOException {
        var tmpJsonFilePath = Files.createTempFile("fin-", ".json");
        var parser = AbstractMT.parse(finFilePath.toAbsolutePath().toFile());
        var json = parser.toJson();
        log.trace("Parsing results:\n{}", json);
        Files.writeString(tmpJsonFilePath, json);
        tmpJsonFilePath.toFile().deleteOnExit();
        return tmpJsonFilePath;
    }


    private Stream<Path> finBatchToProwide(Path inputFilePath, String delimiter) throws IOException {
        final var filesToClean = new ArrayList<Path>();
        var streamOfFiles = doSplitAndMap(inputFilePath, delimiter, path -> {
            try {
                var out = this.finToProwide(path);
                filesToClean.add(out);
                return out;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return streamOfFiles.onClose( () ->
                FileUtils.silentDelete(filesToClean));
    }


    private <S> Stream<S> doSplitAndMap(Path inputFilePath, String delimiter, Function<Path, S> consumer) throws IOException {
        var splitter = new FileSplitter(delimiter, ".part");
        var stream = splitter.split(inputFilePath)
                .map(consumer);
        return stream.onClose(splitter::dispose);
    }

}
