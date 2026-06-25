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

    /**
     * Transaction type
     */
    private String type = "SDK";

    /**
     * Intermediaries
     */
    private List<SimpleTransactionParty> intermediaries = new ArrayList<>();

    /**
     * Payment reference
     */
    private String paymentRef;

    /**
     * Transaction amount
     */
    private BigDecimal amount;

    /**
     * Transaction currency
     */
    private String currency;

    /**
     * Transaction debtor
     */
    private SimpleTransactionParty debtor;

    /**
     * Transaction creditor
     */
    private SimpleTransactionParty creditor;

    /**
     * Transaction purpose
     */
    private String purpose;

}
