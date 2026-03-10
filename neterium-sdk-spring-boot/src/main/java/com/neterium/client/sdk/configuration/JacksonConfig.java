package com.neterium.client.sdk.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


/**
 * Configuration of Jackson
 *
 * @author Bernard Ligny
 */
@Configuration(proxyBeanMethods = false)
public class JacksonConfig {

    /**
     * Constructor
     */
    public JacksonConfig() {
    }


    /**
     * Expose a pre-configured <code>ObjectMapper</code> instance for JSON management
     *
     * @return a <code>ObjectMapper</code> instance
     */
    @Bean
    @Primary
    public ObjectMapper jsonMapper() {
        return doConfigure(new ObjectMapper());
    }


    /**
     * Expose a pre-configured <code>ObjectMapper</code> instance for XML management
     *
     * @return a pre-configured <code>XmlMapper</code> instance
     */
    @Bean
    public XmlMapper xmlMapper() {
        return doConfigure(new XmlMapper());
    }


    @SuppressWarnings("unchecked")
    private <T extends ObjectMapper> T doConfigure(T mapper, Module... extraModules) {
        return (T) mapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(JsonParser.Feature.USE_FAST_BIG_NUMBER_PARSER, true)
                .configure(JsonParser.Feature.USE_FAST_DOUBLE_PARSER, true)
                .configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)
                .findAndRegisterModules();
    }

}
