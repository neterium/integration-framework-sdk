package com.neterium.client.sdk.converters;

import com.neterium.client.sdk.model.ScreenableTransaction;

import java.math.BigDecimal;
import java.util.List;

/**
 * A {@link ScreenableTransaction} exposing setter methods.<p>
 * Implementing this interface ensures that the mapping components will be able to find
 * appropriate setter of transaction properties when applying the mapping.
 *
 * @param <P> concrete type of screenable transaction party
 * @author Bernard Ligny
 * @see JetFlowConverterSupport
 */
public interface ScreenableTransactionBean<P extends ScreenableTransactionBean.PartyBean>
        extends ScreenableTransaction<P> {

    /**
     * Set payment reference
     *
     * @param ref payment reference
     */
    void setPaymentRef(String ref);

    /**
     * Set transaction amount
     *
     * @param amount transaction amount
     */
    void setAmount(BigDecimal amount);

    /**
     * Set transaction currency
     *
     * @param currency transaction currency
     */
    void setCurrency(String currency);

    /**
     * Set debtor party
     *
     * @param party debtor party
     */
    void setDebtor(P party);

    /**
     * Set creditor party
     *
     * @param party creditor party
     */
    void setCreditor(P party);

    /**
     * Set transaction type
     *
     * @param type transaction type
     */
    void setType(String type);

    /**
     * Set transaction purpose
     *
     * @param purpose transaction purpose
     */
    void setPurpose(String purpose);

    /**
     * Set intermediaries
     *
     * @param parties intermediaries
     */
    void setIntermediaries(List<P> parties);

    /**
     * Party bean
     */
    interface PartyBean extends ScreenableTransaction.Party {

        /**
         * Set account number
         *
         * @param accountNumber account number
         */
        void setAccountNumber(String accountNumber);

        /**
         * Set account type
         *
         * @param accountType account type
         */
        void setAccountType(String accountType);

        /**
         * Set full name
         *
         * @param fullName full name
         */
        void setFullName(String fullName);

        /**
         * Set address line
         *
         * @param addressLine address line
         */
        void setAddressLine(String addressLine);

        /**
         * Set postal code of address
         *
         * @param addressPostalCode postal code of address
         */
        void setAddressPostalCode(String addressPostalCode);

        /**
         * Set city of address
         *
         * @param addressCity city of address
         */
        void setAddressCity(String addressCity);

        /**
         * Set country code
         *
         * @param countryCode country code
         */
        void setCountryCode(String countryCode);

        /**
         * Set party label
         *
         * @param label party label
         */
        void setLabel(String label);

        /**
         * Set party identifier
         *
         * @param id party identifier
         */
        void setIdValue(String id);

        /**
         * Set type of party identifier
         *
         * @param type type of party identifier
         */
        void setIdType(String type);

        /**
         * Set additional text
         *
         * @param text additional text
         */
        void setText(String text);
    }

}
