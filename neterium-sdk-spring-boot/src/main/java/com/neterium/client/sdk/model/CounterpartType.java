package com.neterium.client.sdk.model;

/**
 * Type of counterpart
 *
 * @author Bernard Ligny
 */
public enum CounterpartType implements BaseEnum {

    /**
     * Individual
     */
    INDIVIDUAL("individual"),

    /**
     * Entity (eg company, etc...)
     */
    ENTITY("entity");

    final String code;

    /**
     * Constructor
     *
     * @param code code value
     */
    CounterpartType(String code) {
        this.code = code;
    }


    /**
     * @see BaseEnum#getInternalCode()
     */
    @Override
    public String getInternalCode() {
        return code;
    }

}
