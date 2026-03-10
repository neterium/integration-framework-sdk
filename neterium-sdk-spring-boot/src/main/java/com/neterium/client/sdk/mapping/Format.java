package com.neterium.client.sdk.mapping;

import lombok.Getter;

/**
 * Formats of file that supported by the {@link DeclarativeMapper}
 * with the corresponding mapping file
 *
 * @author Bernard Ligny
 */
@Getter
public enum Format {

    /**
     * ISO PACS-008 form
     */
    PACS_OO8("pacs008.yaml", ".xml"),

    /**
     * SWIFT FIN MT-103
     */
    FIN_MT103("fin-mt103.yaml", ".fin"),

    /**
     * SWIFT FIN MT-541
     */
    FIN_MT541("fin-mt541.yaml", ".fin");

    private final String mappingFile;
    private final String fileExt;

    /**
     * Constructor
     *
     * @param mappingFile file containing the mapping logic
     * @param fileExt     extension of data files
     */
    Format(String mappingFile, String fileExt) {
        this.mappingFile = mappingFile;
        this.fileExt = fileExt;
    }

}
