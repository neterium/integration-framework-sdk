package com.neterium.client.sdk.privatelist;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * All possible fields that can appear in a private list in CSV format
 *
 * @author Bernard Ligny
 */
@Getter
@RequiredArgsConstructor
public enum ListType {

    /**
     * Regular list of entities
     */
    ENTITY_LIST(Set.of(
            CsvField.ID,
            CsvField.NAME,
            CsvField.TYPE,
            CsvField.ALIASES,
            CsvField.GENDER,
            CsvField.IDS,
            CsvField.CATS,
            CsvField.DOBS,
            CsvField.POBS,
            CsvField.ADR,
            CsvField.NATS,
            CsvField.CTZS,
            CsvField.VSL,
            CsvField.ACTION
    )),

    /**
     * List with custom ids, keywords, risk countries or regions
     */
    CUSTOM_LIST(Set.of(
            CsvField.ID,
            CsvField.NAME,
            CsvField.PROG,
            CsvField.ALIASES,
            CsvField.IDS
    ));

    private final Set<CsvField> allowedFields;

}
