package com.neterium.client.sdk.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.neterium.client.sdk.mapping.yaml.MappingFunction;
import com.neterium.client.sdk.mapping.yaml.Mappings;
import com.neterium.client.sdk.utils.IteratorSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.ObjectUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A mapper engine where mapping rules are defined in an external file using JSONPath syntax.
 * Input data files are parsed with Jackson {@link ObjectMapper} (and subclasses such as XmlMapper or CSVMapper),
 * meaning JSON, XML and CSV formats are supported.
 *
 * @param <T> the type of the target beans
 * @author Bernard Ligny
 */
@Slf4j
public class DeclarativeMapper<T> {

    private final ObjectMapper objectMapper;
    private final Mappings mappings;
    private final ParseContext parseContext;
    private final Class<T> beanType;


    /**
     * Constructor
     *
     * @param objectMapper a pre-configured ObjectMapper instance
     * @param format       the supported file format
     * @param beanType     class of target beans
     */
    public DeclarativeMapper(ObjectMapper objectMapper, Format format, Class<T> beanType) {
        this.objectMapper = objectMapper;
        this.mappings = loadMappingFile(format.getMappingFile());
        this.beanType = beanType;
        this.parseContext = createParseContext();
    }


    /**
     * Parse a file and map data to a stream of populated pojo's
     *
     * @param inputFilePath path to the input file to parse
     * @return a stream of T instances
     * @throws IOException in case of IO exception
     */
    public Stream<T> parseFile(Path inputFilePath) throws IOException {
        var inputStream = new FileInputStream((inputFilePath.toFile()));
        var iterator = parseInputStream(inputStream);
        return StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
                .onClose(() -> {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }


    /**
     * Parse an input stream and map data to a stream of populated pojo's
     *
     * @param inputStream input data stream to parse
     * @return a stream of T instances
     */
    public Iterator<T> parseInputStream(InputStream inputStream) {
        try {
            var parser = objectMapper.getFactory().createParser(inputStream);
            var nodeSelector = new FilteringIterator(parser, mappings.getFragmentNames());
            var allContexts = new HashMap<String, DocumentContext>();
            return new IteratorSupport<>() {
                @Override
                protected T readNextValue() {
                    return readNext(nodeSelector, allContexts);
                }
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private T readNext(Iterator<FilteringIterator.Entry> nodeIterator,
                       Map<String, DocumentContext> allContexts) {
        var fragmentNames = mappings.getFragmentNames();
        var primaryFragment = mappings.getPrimaryFragment();
        while (nodeIterator.hasNext()) {
            var next = nodeIterator.next();
            var nodeName = next.name();
            if (fragmentNames.contains(nodeName)) {
                var ctx = parseContext.parse(next.node());
                allContexts.put(nodeName, ctx);
                if (primaryFragment.equals(nodeName)) {
                    T instance;
                    try {
                        instance = beanType.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    BeanWrapper bw = new BeanWrapperImpl(instance);
                    bw.setAutoGrowNestedPaths(true);
                    applyMapping(bw, primaryFragment, allContexts);
                    return instance;
                }
            }
        }
        return null;
    }


    private ParseContext createParseContext() {
        var jsonPathConfig = Configuration.builder()
                .jsonProvider(new JacksonJsonNodeJsonProvider())
                .mappingProvider(new JacksonMappingProvider())
                .options(Option.DEFAULT_PATH_LEAF_TO_NULL, Option.SUPPRESS_EXCEPTIONS, Option.ALWAYS_RETURN_LIST)
                .build();
        return JsonPath.using(jsonPathConfig);
    }


    private Mappings loadMappingFile(String mappingFile) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream(mappingFile);
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(is, Mappings.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read mapping file", e);
        }
    }


    private void applyMapping(BeanWrapper bw, String primaryFragment, Map<String, DocumentContext> allContexts) {
        for (var targetProperty : mappings.getMappings().keySet()) {
            var fieldMapping = mappings.getMappings().get(targetProperty);
            Class<?> dataType = fieldMapping.getType().getJavaType();
            var sourceFragment = fieldMapping.getFragment()
                    .orElse(primaryFragment);
            var bindingCtx = allContexts.get(sourceFragment);
            List<?> arguments;
            if (fieldMapping.getFunction().isDynamic()) {
                arguments = fieldMapping.getSources()
                        .stream()
                        .map(path -> bindingCtx.read(path, listOf(dataType)))
                        .map(list -> ObjectUtils.isEmpty(list) ? null : list.getFirst())
                        .toList();
            } else {
                arguments = fieldMapping.getSources();
            }
            Object targetValue = null;
            if (fieldMapping.getFunction().equals(MappingFunction.LOOKUP)) {
                var index = fieldMapping.getFunction().apply(arguments, Integer.class);
                if (index>=0) {
                    targetValue = fieldMapping.getValues().get(index);
                }
            } else {
                targetValue = fieldMapping.getFunction().apply(arguments, dataType);
            }
            log.trace("Setting {} to {} on {} instance", targetProperty, targetValue, bw.getWrappedInstance().getClass().getSimpleName());
            bw.setPropertyValue(targetProperty, targetValue);
        }
        allContexts.clear();
    }


    private <E> TypeRef<List<E>> listOf(Class<E> elementType) {

        ParameterizedType listType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{elementType};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };

        return new TypeRef<List<E>>() {
            @Override
            public Type getType() {
                return listType;
            }
        };
    }

}
