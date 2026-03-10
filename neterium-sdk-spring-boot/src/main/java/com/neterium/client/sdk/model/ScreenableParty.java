package com.neterium.client.sdk.model;

/**
 * Models a screenable counterpart.<p>
 * Implementing this interface ensures that screening components will be able to find
 * appropriate getter of counterpart properties when populating screening requests.
 *
 * @author Bernard Ligny
 */
public interface ScreenableParty extends Screenable {

    /**
     * Get party identifier
     *
     * @return party identifier
     */
    String getId();

    /**
     * Get last name
     *
     * @return last name
     */
    String getLastName();

    /**
     * Get first name
     *
     * @return first name
     */
    String getFirstName();

    /**
     * Get party type (eg individual, entity, ...)
     *
     * @return party type
     */
    BaseEnum getType();


    /**
     * Get middle names
     *
     * @return middle names
     */
    default String getMiddleNames() {
        return null;
    }

    /**
     * Get code of registration country
     *
     * @return code of registration country
     */
    default String getRegistrationCountryCode() {
        return null;
    }

    /**
     * Get registration number
     *
     * @return registration number
     */
    default String getRegistrationNumber() {
        return null;
    }

    /**
     * Get gender
     *
     * @return gender
     */
    default BaseEnum getGender() {
        return null;
    }

    /**
     * Get date of birth
     *
     * @return date of birth
     */
    default String getDateOfBirth() {
        return null;
    }

    /**
     * Get code of address country
     *
     * @return code of address country
     */
    default String getAddressCountryCode() {
        return null;
    }

    /**
     * Get city of address
     *
     * @return city of address
     */
    default String getAddressCityName() {
        return null;
    }

}
