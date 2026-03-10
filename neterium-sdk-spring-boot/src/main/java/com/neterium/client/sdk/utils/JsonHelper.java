package com.neterium.client.sdk.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * A helper component to serialize &amp; unserialize JSON payloads using Jackson library
 *
 * @author Bernard Ligny
 */
@Component
@Slf4j
public class JsonHelper {

    private final ObjectMapper mapper;


    /**
     * Constructor
     *
     * @param mapper a pre-configured Jackson <code>ObjectMapper</code>
     */
    public JsonHelper(@Lazy ObjectMapper mapper) {
        this.mapper = mapper.copy()
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
    }


    /**
     * Serialize an object to a JSON string
     *
     * @param object the object to serialize
     * @return JSON representation of the object
     */
    public String serialize(Object object) {
        return serialize(object, false, true);
    }


    /**
     * Serialize an object to a JSON string
     *
     * @param object      the object to serialize
     * @param pretty      whether to pretty print JSON string
     * @param ignoreError whether to ignore serialization errors or not
     * @return SON representation of the object
     */
    public String serialize(Object object, boolean pretty, boolean ignoreError) {
        try {
            if (pretty) {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            } else {
                return mapper.writeValueAsString(object);
            }
        } catch (JsonProcessingException e) {
            log.warn("Error while serializing to json", e);
            if (ignoreError) {
                return null;
            } else {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * Deserialize JSON content from given file into given Java type
     *
     * @param file        the JSON file to read
     * @param type        the target class
     * @param ignoreError whether to ignore deserialization errors or not
     * @param <T>         type of target data
     * @return an instance of T
     */
    public <T> T deserializeFile(File file, Class<T> type, boolean ignoreError) {
        try {
            return mapper.readValue(file, type);
        } catch (IOException e) {
            log.warn("Error while serializing to json", e);
            if (ignoreError) {
                return null;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

}
