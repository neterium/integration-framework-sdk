package com.neterium.client.sdk.converters;

import com.neterium.client.sdk.mapping.Format;
import com.neterium.sdk.model.JetFlowRequestBody;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * A converter to prepare screening requests to feed JetFlow
 *
 * @author Bernard Ligny
 */
public interface JetFlowConverter {

    /**
     * Test whether this converter is able to handle a file format
     *
     * @param format the candidate file format
     * @return true if supported by this converter, false otherwise
     */
    boolean handle(Format format);

    /**
     * Convert a source file to a stream of {@link JetFlowRequestBody} objects
     *
     * @param inputFilePath the input file to convert
     * @param batchSize     the desired batch size
     * @return a stream of {@link JetFlowRequestBody} objects
     * @throws IOException in case of I/O exception
     */
    Stream<JetFlowRequestBody> convert(Path inputFilePath, int batchSize) throws IOException;


    /**
     * Convert a source batch file to a stream of {@link JetFlowRequestBody} objects
     *
     * @param inputFilePath the input batch file to convert
     * @param delimiter     the delimiter
     * @param batchSize     the desired batch size
     * @return a stream of {@link JetFlowRequestBody} objects
     * @throws IOException in case of I/O exception
     */
    Stream<JetFlowRequestBody> convertBatch(Path inputFilePath, String delimiter, int batchSize) throws IOException;


    /**
     * Convert a source file to a stream of serialized {@link JetFlowRequestBody} objects
     *
     * @param inputFilePath the input file to convert
     * @param batchSize     the desired batch size
     * @param pretty        if JSON needs to be pretty formatted
     * @return a stream of JSON strings
     * @throws IOException in case of I/O exception
     */
    Stream<String> convertAndSerialize(Path inputFilePath, int batchSize, boolean pretty) throws IOException;


    /**
     * Convert a source batch file to a stream of serialized {@link JetFlowRequestBody} objects
     *
     * @param inputFilePath the input batch file to convert
     * @param delimiter     the delimiter
     * @param batchSize     the desired batch size
     * @param pretty        if JSON needs to be pretty formatted
     * @return a stream of JSON strings
     * @throws IOException in case of I/O exception
     */
    Stream<String> convertAndSerializeBatch(Path inputFilePath, String delimiter, int batchSize, boolean pretty) throws IOException;

}
