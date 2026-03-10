package com.neterium.client.sdk.converters;

import lombok.Data;

/**
 * A default and basic implementation of a {@link ScreenableTransactionBean.PartyBean}
 *
 * @author Bernard Ligny
 */
@Data
public class SimpleTransactionParty implements ScreenableTransactionBean.PartyBean {

    private String accountNumber;
    private String accountType;
    private String fullName;
    private String addressLine;
    private String addressPostalCode;
    private String addressCity;
    private String countryCode;
    private String label;
    private String idValue;
    private String idType;
    private String text;

}
