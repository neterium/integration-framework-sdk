package com.neterium.client.sdk.batch.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neterium.client.sdk.mapping.DeclarativeMapper;
import com.neterium.client.sdk.mapping.Format;
import com.prowidesoftware.swift.model.mt.AbstractMT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Iterator;


/**
 * Base abstract class for an <code> ItemStreamReader</code> implementation which needs to
 * read &amp; map data using some declarative mapping rules that are derived from the supplied format.
 *
 * @param <T> type of target model
 * @author Bernard Ligny
 * @see DeclarativeMapper
 */
@Slf4j
public abstract class AbstractFileMapper<T> extends AbstractItemCountingItemStreamItemReader<T>
        implements ResourceAwareItemReaderItemStream<T> {

    private ObjectMapper objectMapper;
    private Class<T> beanType;
    private Format format;
    private Resource resource;
    private DeclarativeMapper<T> declarativeMapper;
    private InputStream inputStream;
    private Iterator<T> dataStream;


    /**
     * Constructor
     */
    protected AbstractFileMapper() {
        super();
        super.setSaveState(false);
    }


    /**
     * Check configuration
     */
    protected void afterPropertiesSet() {
        Assert.state(!ObjectUtils.isEmpty(objectMapper), "The objectMapper must not be empty");
        Assert.state(!ObjectUtils.isEmpty(format), "The format must not be empty");
        Assert.state(!ObjectUtils.isEmpty(beanType), "The beanType must not be empty");
        this.declarativeMapper = new DeclarativeMapper<>(objectMapper, format, beanType);
    }


    /**
     * Set the input resource to read
     *
     * @param resource resource to read
     */
    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
    }


    /**
     * Set the format of the resource
     *
     * @param format resource format
     */
    protected void setFormat(Format format) {
        this.format = format;
    }


    /**
     * Set the appropriate (json or xml) ObjectMapper used to read data
     *
     * @param objectMapper a Jackson {@link ObjectMapper} instance
     */
    protected void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    
    /**
     * Set the type (class) of the target model
     *
     * @param beanType type of target model
     */
    protected void setBeanType(Class<T> beanType) {
        this.beanType = beanType;
    }


    /**
     * Open resources necessary to start reading input
     */
    @Override
    protected void doOpen() throws Exception {
        Assert.notNull(this.resource, "Resource must not be null.");
        if (!this.resource.exists()) {
            log.warn("Input resource does not exist {}", this.resource.getDescription());
        } else if (!this.resource.isReadable()) {
            log.warn("Input resource is not readable {}", this.resource.getDescription());
        } else {
            this.inputStream = getInputStream();
            this.dataStream = declarativeMapper.parseInputStream(inputStream);
        }
    }


    /**
     * Read next item from input
     *
     * @return a T-item or null if the data source is exhausted
     */
    @Override
    protected T doRead() {
        if (dataStream.hasNext()) {
            return dataStream.next();
        } else {
            return null;
        }
    }


    /**
     * Close the resources opened in {@link #doOpen()}
     */
    @Override
    protected void doClose() throws Exception {
        inputStream.close();
    }


    /*
     * Prepare the inputstream to read (with Jackson ObjectMapper)
     */
    private InputStream getInputStream() throws IOException {
        if (format.equals(Format.PACS_OO8)) {
            // XML data can be directly parsed
            return this.resource.getInputStream();
        } else {
            // A FIN->JSON conversion is needed before being parsed
            var parser = AbstractMT.parse(this.resource.getInputStream());
            var tmpJsonFilePath = Files.createTempFile("fin-", ".json");
            Files.writeString(tmpJsonFilePath, parser.toJson());
            var outFile = tmpJsonFilePath.toFile();
            outFile.deleteOnExit();
            return new FileInputStream(outFile);
        }
    }

}
