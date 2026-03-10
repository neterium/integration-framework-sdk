package com.neterium.client.sdk.converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neterium.client.sdk.binding.JetFlowBinder;
import com.neterium.client.sdk.utils.JsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import static com.neterium.client.sdk.mapping.Format.FIN_MT541;

/**
 * SwiftFinConverter for MT-541 messages
 *
 * @author Bernard Ligny
 */
@Component
@Lazy
@Slf4j
public class SwiftFinMT541Converter extends SwiftFinConverter {

    /**
     * Constructor
     *
     * @param jsonMapper a Jackson <code>ObjectMapper</code> instance
     * @param binder     a {@link JetFlowBinder} instance
     * @param jsonHelper a {@link JsonHelper} instance
     */
    public SwiftFinMT541Converter(ObjectMapper jsonMapper,
                                  JetFlowBinder binder,
                                  JsonHelper jsonHelper) {
        super(jsonMapper, FIN_MT541, binder, jsonHelper, SimpleTransaction.class);
    }

}
