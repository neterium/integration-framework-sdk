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

    ID("uid"),
    NAME(List.of("lastName", "firstName"), false, false),
    TYPE("sdnType"),
    ALIASES(List.of("akaList.aka[%d].lastName", "akaList.aka[%d].firstName"), true, false),
    GENDER(List.of("idList.id[%d]"), false, true, "idType", "idNumber"),
    IDS(List.of("idList.id[%d]"), true, true, "idType", "idNumber"),
    CATS(List.of("programList.program[%d]"), true, false),
    DOBS(List.of("dateOfBirthList.dateOfBirthItem[%d].dateOfBirth"), true, false),
    POBS(List.of("placeOfBirthList.placeOfBirthItem[%d].placeOfBirth"), true, false),
    ADR(List.of("addressList.address[%d].city", "addressList.address[%d].country"), true, false),
    NATS(List.of("nationalityList.nationality[%d].country"), true, false),
    CTZS(List.of("citizenshipList.citizenship[%d].country"), true, false),
    VSL(List.of("vesselInfo"), true, true),
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
