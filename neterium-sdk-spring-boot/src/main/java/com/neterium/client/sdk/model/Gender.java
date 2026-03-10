package com.neterium.client.sdk.model;

/**
 * Gender enum
 *
 * @author Bernard Ligny
 */
public enum Gender implements BaseEnum {

    /**
     * Male gender
     */
    MALE("male"),

    /**
     * Female gender
     */
    FEMALE("female"),

    /**
     * Other gender
     */
    OTHER("other");

    final String code;

    /**
     * Constructor
     *
     * @param code code value
     */
    Gender(String code) {
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
