package com.neterium.client.sdk.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * Models a screenable transaction.<p>
 * Implementing this interface ensures that screening components will be able to find
 * appropriate getter of transaction properties when populating screening requests.
 *
 * @param <P> concrete type of screenable transaction party
 * @author Bernard Ligny
 */
public interface ScreenableTransaction<P extends ScreenableTransaction.Party> extends Screenable {

    /**
     * Get payment reference
     *
     * @return the payment reference
     */
    String getPaymentRef();

    /**
     * Get transaction amount
     *
     * @return the transaction amount
     */
    BigDecimal getAmount();

    /**
     * Get transaction currency
     *
     * @return the transaction currency
     */
    String getCurrency();

    /**
     * Get debtor party
     *
     * @return the debtor party
     */
    P getDebtor();

    /**
     * Get creditor party
     *
     * @return the creditor party
     */
    P getCreditor();

    /**
     * Get transaction type
     *
     * @return the transaction type
     */
    default String getType() {
        return null;
    }

    /**
     * Get transaction purpose
     *
     * @return the transaction purpose
     */
    default String getPurpose() {
        return null;
    }

    /**
     * Get intermediaries
     *
     * @return the intermediaries
     */
    default List<P> getIntermediaries() {
        return Collections.emptyList();
    }


    /**
     * A Party involved in a transaction
     */
    interface Party {

        /**
         * Get account number
         *
         * @return account number
         */
        default String getAccountNumber() {
            return null;
        }

        /**
         * Get account type
         *
         * @return account type
         */
        default String getAccountType() {
            return null;
        }

        /**
         * Get full name
         *
         * @return full name
         */
        default String getFullName() {
            return null;
        }

        /**
         * Get address line
         *
         * @return address line
         */
        default String getAddressLine() {
            return null;
        }

        /**
         * Get postal code of address
         *
         * @return postal code
         */
        default String getAddressPostalCode() {
            return null;
        }

        /**
         * Get city of address
         *
         * @return city
         */
        default String getAddressCity() {
            return null;
        }

        /**
         * Get country code
         *
         * @return country code
         */
        default String getCountryCode() {
            return null;
        }

        /**
         * Get party label
         *
         * @return party label
         */
        default String getLabel() {
            return null;
        }

        /**
         * Get party identifier
         *
         * @return party identifier
         */
        default String getIdValue() {
            return null;
        }

        /**
         * Get type of party identifier
         *
         * @return type of party identifier
         */
        default String getIdType() {
            return null;
        }

        /**
         * Get additional text
         *
         * @return additional text
         */
        default String getText() {
            return null;
        }
    }

}
