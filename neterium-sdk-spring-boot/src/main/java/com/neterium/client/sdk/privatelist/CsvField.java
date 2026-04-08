package com.neterium.client.sdk.privatelist;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * All possible fields that can appear in a private list in CSV format
 *
 * @author Bernard Ligny
 */
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public enum CsvField {

    /**
     * Record identifier
     */
    ID("uid"),

    /**
     * Full name in the format "LastName, FirstName"
     */
    NAME(List.of("lastName", "firstName"), false, false),

    /**
     * Type of party (Individual, Entity, etc...)
     */
    TYPE("sdnType"),

    /**
     * List of aliases in the format "LastName, FirstName" (multivalued)
     */
    ALIASES(List.of("akaList.aka[%d].lastName", "akaList.aka[%d].firstName"), true, false),

    /**
     * Gender (in case of Individual type)
     */
    GENDER(List.of("idList.id[%d]"), false, true, "idType", "idNumber"),

    /**
     * List of party identifiers in the format "Type=Value" (multivalued, dictionary)
     */
    IDS(List.of("idList.id[%d]"), true, true, "idType", "idNumber"),

    /**
     * List of categories (multivalued)
     */
    CATS(List.of("programList.program[%d]"), true, false),

    /**
     * List of date-of-birth (multivalued)
     */
    DOBS(List.of("dateOfBirthList.dateOfBirthItem[%d].dateOfBirth"), true, false),

    /**
     * List of place-of-birth (multivalued)
     */
    POBS(List.of("placeOfBirthList.placeOfBirthItem[%d].placeOfBirth"), true, false),

    /**
     * List of addresses in the format "City, Country" (multivalued)
     */
    ADR(List.of("addressList.address[%d].city", "addressList.address[%d].country"), true, false),

    /**
     * List of nationalities (multivalued)
     */
    NATS(List.of("nationalityList.nationality[%d].country"), true, false),

    /**
     * List of citizenships (multivalued)
     */
    CTZS(List.of("citizenshipList.citizenship[%d].country"), true, false),

    /**
     * List of vessel information in the format "Name=Value" (multivalued, dictionary)
     */
    VSL(List.of("vesselInfo"), true, true),

    /**
     * Action on record
     */
    ACTION("action");

    private final List<String> properties;
    private final boolean multiValued;
    private final boolean dictionary;
    private String keyProperty;
    private String valueProperty;

    /**
     * Constructor
     */
    CsvField(String path) {
        this(List.of(path), false, false);
    }

    /**
     * Indicates whether the field value has to be split across multiple target properties
     *
     * @return boolean
     */
    public boolean needSplit() {
        return properties.size() == 2;
    }

}
