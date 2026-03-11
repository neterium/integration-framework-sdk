package com.neterium.client.sdk.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neterium.client.sdk.binding.JetFlowBinder;
import com.neterium.client.sdk.mapping.DeclarativeMapper;
import com.neterium.client.sdk.mapping.Format;
import com.neterium.client.sdk.utils.BatchHelper;
import com.neterium.client.sdk.utils.FileSplitter;
import com.neterium.client.sdk.utils.JsonHelper;
import com.neterium.sdk.model.CoreRequestContext;
import com.neterium.sdk.model.CoreScreenOptions;
import com.neterium.sdk.model.JetFlowRequestBody;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Base class that can be used as support of a {@link JetFlowConverter} implementation.
 * Most of the job will be delegated to the underlying
 * <ul>
 * <li>{@link DeclarativeMapper} for the mapping phase</li>
 * <li>{@link JetFlowBinder} for the binding phase</li>
 * </ul>
 *
 * @param <T> The concrete type of transaction beans
 * @author Bernard Ligny
 */
@Slf4j
public abstract class JetFlowConverterSupport<T extends ScreenableTransactionBean<?>> implements JetFlowConverter {

    private final DeclarativeMapper<T> mapper;
    private final JetFlowBinder binder;
    private final JsonHelper jsonHelper;
    private final List<UnaryOperator<JetFlowRequestBody>> enrichers;


    /**
     * Constructor
     *
     * @param jacksonMapper an <code>ObjectMapper</code> instance
     * @param format        format of the input transactions
     * @param binder        a {@link JetFlowBinder} instance
     * @param jsonHelper    a {@link JsonHelper} instance
     * @param beanType      concrete type of transaction beans
     */
    public JetFlowConverterSupport(ObjectMapper jacksonMapper,
                                   Format format,
                                   JetFlowBinder binder,
                                   JsonHelper jsonHelper,
                                   Class<T> beanType) {
        this.mapper = new DeclarativeMapper<>(jacksonMapper, format, beanType);
        this.binder = binder;
        this.enrichers = new ArrayList<>();
        this.jsonHelper = jsonHelper;
    }


    /**
     * Configure this converter to enrich all resulting {@link JetFlowRequestBody}'s
     * with some enrichment operation
     *
     * @param operator the enricher
     */
    public void addEnricher(UnaryOperator<JetFlowRequestBody> operator) {
        this.enrichers.add(operator);
    }


    /**
     * Configure this converter to enrich all resulting {@link JetFlowRequestBody}'s
     * by filling the request context with the supplied JSON data
     *
     * @param json path to the JSON file
     */
    public void addContextEnricher(Path json) {
        final CoreRequestContext context = jsonHelper.deserializeFile(json.toFile(), CoreRequestContext.class, false);
        this.enrichers.add(jetFlowRequestBody ->
                jetFlowRequestBody.context(context));
    }


    /**
     * Configure this converter to enrich all resulting {@link JetFlowRequestBody}'s
     * by filling the request options with the supplied JSON data
     *
     * @param json path to the JSON file
     */
    public void addOptionEnricher(Path json) {
        final CoreScreenOptions options = jsonHelper.deserializeFile(json.toFile(), CoreScreenOptions.class, false);
        this.enrichers.add(jetFlowRequestBody ->
                jetFlowRequestBody.options(options));
    }


    /**
     * @see JetFlowConverter#convert(Path, int)
     */
    @Override
    public Stream<JetFlowRequestBody> convert(Path inputFilePath, int batchSize) throws IOException {
        return doConvert(Stream.of(inputFilePath), batchSize);
    }


    /**
     * @see JetFlowConverter#convertAndSerialize(Path, int, boolean)
     */
    @Override
    public Stream<String> convertAndSerialize(Path inputFilePath, int batchSize, boolean pretty) throws IOException {
        return doConvertAndSerialize(Stream.of(inputFilePath), batchSize, pretty);
    }


    /**
     * @see JetFlowConverter#convertBatch(Path, String, int)
     */
    @Override
    public Stream<JetFlowRequestBody> convertBatch(Path inputFilePath, String delimiter, int batchSize) throws IOException {
        return doSplit(inputFilePath, delimiter, streamOfTmpFiles -> {
            try {
                return doConvert(streamOfTmpFiles, batchSize);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    /**
     * @see JetFlowConverter#convertAndSerializeBatch(Path, String, int, boolean)
     */
    @Override
    public Stream<String> convertAndSerializeBatch(Path inputFilePath, String delimiter, int batchSize, boolean pretty) throws IOException {
        return doSplit(inputFilePath, delimiter, streamOfTmpFiles -> {
            try {
                return doConvertAndSerialize(streamOfTmpFiles, batchSize, pretty);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    private Stream<JetFlowRequestBody> doConvert(Stream<Path> inputFilePaths, int batchSize) throws IOException {
        Stream<T> stream = Stream.of();
        var it = inputFilePaths.iterator();
        while (it.hasNext()) {
            var transactionsStream = doConvert(it.next());
            stream = Stream.concat(stream, transactionsStream);
        }
        return BatchHelper.groupIntoBatches(stream, batchSize)
                .map(binder::bind)
                .map(this::doEnrich);
    }


    protected Stream<T> doConvert(Path inputFilePath) throws IOException {
        return mapper.parseFile(inputFilePath);
    }


    private Stream<String> doConvertAndSerialize(Stream<Path> inputFilePaths, int batchSize, boolean pretty) throws IOException {
        return doConvert(inputFilePaths, batchSize)
                .map(body -> jsonHelper.serialize(body, pretty, true))
                .filter(Objects::nonNull);
    }


    private <S> Stream<S> doSplit(Path inputFilePath, String delimiter, Function<Stream<Path>, Stream<S>> consumer) throws IOException {
        FileSplitter splitter = null;
        try {
            splitter = new FileSplitter(delimiter, ".part");
            var stream = splitter.split(inputFilePath);
            return consumer.apply(stream);
        } finally {
            if (splitter != null) splitter.dispose();
        }
    }


    private JetFlowRequestBody doEnrich(JetFlowRequestBody jetFlowRequestBody) {
        for (var enricher : enrichers) {
            jetFlowRequestBody = enricher.apply(jetFlowRequestBody);
        }
        return jetFlowRequestBody;
    }

}
