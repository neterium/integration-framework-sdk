package com.neterium.client.sdk.mapping.yaml;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Possible data types for a {@link MappingEntry} with the corresponding Java type
 *
 * @author Bernard Ligny
 */
@Getter
public enum DataType {

    /**
     * String type
     */
    STRING(String.class),

    /**
     * BigDecimal type
     */
    BIG_DECIMAL(BigDecimal.class);


    private final Class<?> javaType;

    /**
     * Constructor
     *
     * @param javaType corresponding Java type
     */
    DataType(Class<?> javaType) {
        this.javaType = javaType;
    }

}
