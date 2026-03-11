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

    /**
     * Token used to separate records in a SWIFT FIN batch file
     */
    public static final String DELIMITER = "-}$";


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



    @Override
    protected Stream<SimpleTransaction> doConvert(Path inputFilePath) throws IOException {
        // First convert regular file to intermediate format
        var tmpJsonFilePath = finToProwide(inputFilePath);
        // Then use jetflow generic converter
        var stream = super.doConvert(tmpJsonFilePath);
        return stream.onClose(() -> {
            try {
                Files.deleteIfExists(tmpJsonFilePath);
            } catch (IOException ignored) {
            }
        });
    }


    private Path finToProwide(Path finFilePath) throws IOException {
        var tmpJsonFilePath = Files.createTempFile("fin-", ".json");
        var parser = AbstractMT.parse(finFilePath.toAbsolutePath().toFile());
        var json = parser.toJson();
        log.trace("Intermediate parsing results:\n{}", json);
        Files.writeString(tmpJsonFilePath, json);
        tmpJsonFilePath.toFile().deleteOnExit();
        return tmpJsonFilePath;
    }


}
