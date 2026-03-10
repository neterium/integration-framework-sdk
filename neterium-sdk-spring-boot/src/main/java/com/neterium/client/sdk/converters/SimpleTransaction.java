package com.neterium.client.sdk.converters;

import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * A default and basic implementation of a {@link ScreenableTransactionBean}
 *
 * @author Bernard Ligny
 */
@Data
public class SimpleTransaction implements ScreenableTransactionBean<SimpleTransactionParty> {

    private String type = "SDK";
    private List<SimpleTransactionParty> intermediaries = new ArrayList<>();
    private String paymentRef;
    private BigDecimal amount;
    private String currency;
    private SimpleTransactionParty debtor;
    private SimpleTransactionParty creditor;
    private String purpose;

}
