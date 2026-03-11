package com.neterium.client.sdk.converters;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.neterium.client.sdk.binding.JetFlowBinder;
import com.neterium.client.sdk.mapping.Format;
import com.neterium.client.sdk.utils.JsonHelper;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import static com.neterium.client.sdk.mapping.Format.PACS_OO8;

/**
 * A {@link JetFlowConverter} for PACS-008 files
 *
 * @author Bernard Ligny
 */
@Component
@Lazy
public class Pacs008Converter extends JetFlowConverterSupport<SimpleTransaction> {

    /**
     * Token used to separate records in a ISO-2022 batch file
     */
    public static final String DELIMITER = "</xml>";


    /**
     * Constructor
     *
     * @param xmlMapper  a Jackson <code>XmlMapper</code> instance
     * @param binder     a {@link JetFlowBinder} instance
     * @param jsonHelper a {@link JsonHelper} instance
     */
    public Pacs008Converter(XmlMapper xmlMapper,
                            JetFlowBinder binder,
                            JsonHelper jsonHelper) {
        super(xmlMapper, PACS_OO8, binder, jsonHelper, SimpleTransaction.class);
    }


    /**
     * @see JetFlowConverter#handle(Format)
     */
    @Override
    public boolean handle(Format format) {
        return PACS_OO8.equals(format);
    }

}
