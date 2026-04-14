package com.neterium.client.sdk.privatelist;

import com.neterium.client.sdk.exception.SdkException;
import com.neterium.sdk.ofac.ObjectFactory;
import com.neterium.sdk.ofac.SdnList;
import com.neterium.sdk.ofac.SdnList.SdnEntry;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Component that can be used to create private lists from CSV files
 *
 * @author Bernard Ligny
 */
@Slf4j
@Component
public class PrivateListBuilder {

    private static final boolean ADD_UIDS = false;
    private static final DateTimeFormatter IN_FORMATTER = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("dd MMM yyyy",
                    Locale.ENGLISH)) // "13 Feb 1972"
            .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))  // "13/02/1972"
            .appendOptional(DateTimeFormatter.ofPattern("dd-MM-yyyy"))  // "13-02-1972"
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))  // "1972-02-13"
            .appendOptional(DateTimeFormatter.ofPattern("yyyy/MM/dd"))  // "1972/02/13"
            .toFormatter();
    private static final DateTimeFormatter OUT_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy",
            Locale.ENGLISH); // // "13 Feb 1972"


    /**
     * Read &amp; parse a CSV file using default encoding and separators
     *
     * @param csvFile  file to read
     * @param listType type of list
     * @return a private list in canonical XML format
     * @throws Exception in case of error
     */
    public Path parse(File csvFile, ListType listType) throws Exception {
        return parse(csvFile, listType, Charset.defaultCharset(), ';', '~');
    }


    /**
     * Read &amp; parse a CSV file with custom options
     *
     * @param csvFile        file to read
     * @param listType       type of list
     * @param charset        charset of CSV file
     * @param fieldSeparator char used to separate fields (columns)
     * @param valueSeparator char used to separate multiple values in a same field
     * @return a private list in canonical XML format
     * @throws Exception in case of error
     */
    public Path parse(File csvFile, ListType listType, Charset charset, char fieldSeparator, char valueSeparator) throws Exception {
        CSVReader csvReader = null;
        Path outputFile = null;
        SdnList xmlRoot = null;
        try {
            var csvParser = new CSVParserBuilder()
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSeparator(fieldSeparator)
                    .build();
            csvReader = new CSVReaderBuilder(new FileReader(csvFile, charset))
                    .withCSVParser(csvParser)
                    .withKeepCarriageReturn(false)
                    .build();
            List<String> fieldNames = null;
            String[] data;
            int i = 0;
            while ((data = csvReader.readNext()) != null) {
                if (i == 0) {
                    fieldNames = catchHeaders(data, listType);
                    outputFile = Files.createTempFile("tmp_", ".xml");
                    xmlRoot = blankList();
                } else {
                    var entry = processRow(fieldNames, data, valueSeparator);
                    xmlRoot.getSdnEntry().add(entry);
                }
                i++;
            }
            assert (xmlRoot != null);
            finalize(xmlRoot, listType);
            save(xmlRoot, outputFile);
            return outputFile;
        } catch (Exception e) {
            log.error("Error wile reading CSV file", e);
            throw new SdkException(e);
        } finally {
            if (csvReader != null) {
                csvReader.close();
            }
        }
    }


    private List<String> catchHeaders(String[] data, ListType listType) {
        var allowed = listType.getAllowedFields()
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        var headers = new ArrayList<String>();
        for (var headerName : data) {
            var name = headerName.trim().toUpperCase();
            if (!allowed.contains(headerName)) {
                log.error("{} is not a valid header (allowed = {})", headerName, allowed);
                throw new IllegalArgumentException(headerName);
            } else {
                headers.add(name);
            }
        }
        return headers;
    }


    // === Setting bean properties ===

    private SdnEntry processRow(List<String> headers, String[] data, char valueSeparator) {
        var beanWrapper = new SdnEntryWrapper();
        for (int col = 0; col < data.length; col++) {
            var value = data[col];
            if (StringUtils.isEmpty(value)) {
                continue;
            }
            var field = CsvField.valueOf(headers.get(col));
            if (field.isMultiValued()) {
                processMutiValue(field, StringUtils.split(value, valueSeparator), beanWrapper);
            } else {
                var idx = beanWrapper.getSizeIfCollection(field.getProperties().getFirst()).orElse(0);
                processSingleValue(field, field.getProperties(), idx, value, beanWrapper);
            }
        }
        return beanWrapper.unwrap();
    }

    private void processMutiValue(CsvField field, String[] values, SdnEntryWrapper beanWrapper) {
        int offset = beanWrapper.getSizeIfCollection(field.getProperties().getFirst()).orElse(0);
        for (int i = 0; i < values.length; i++) {
            var idx = offset + i;
            processSingleValue(field, field.getProperties(), idx, values[i], beanWrapper);
        }
    }

    private void processSingleValue(CsvField field, List<String> targetProperties, int idx, String value, SdnEntryWrapper beanWrapper) {
        if (field.needSplit()) {
            var tokens = StringUtils.split(value, ",");
            for (int j = 0; j < tokens.length; j++) {
                setValue(field, targetProperties.get(j), idx, tokens[j].trim(), beanWrapper);
            }
        } else {
            setValue(field, targetProperties.getFirst(), idx, value, beanWrapper);
        }
    }

    private void setValue(CsvField field, String targetProperty, int idx, String value, SdnEntryWrapper beanWrapper) {
        targetProperty = targetProperty.formatted(idx);
        if (field.isDictionary()) {
            var tokens = StringUtils.split(value, "=");
            if (tokens.length == 2) {
                if (field.getKeyProperty() == null) {
                    beanWrapper.set(targetProperty + "." + tokens[0], tokens[1]);
                } else {
                    beanWrapper.set(targetProperty + "." + field.getKeyProperty(), tokens[0]);
                    beanWrapper.set(targetProperty + "." + field.getValueProperty(), tokens[1]);
                }
            } else if (field.getKeyProperty() != null && StringUtils.isNotEmpty(value)) {
                beanWrapper.set(targetProperty + "." + field.getKeyProperty(), field.name().toLowerCase());
                beanWrapper.set(targetProperty + "." + field.getValueProperty(), value);
            }
        } else {
            field.getValidation().ifPresent(check -> check.test(value));
            beanWrapper.set(targetProperty, value);
        }
    }


    // === XML management ===

    private SdnList blankList() {
        var factory = new ObjectFactory();
        var root = factory.createSdnList();
        var publishInfo = factory.createSdnListPublshInformation();
        var today = DateTimeFormatter.ofPattern("MM/dd/yyyy").format(LocalDate.now());
        publishInfo.setPublishDate(today);
        root.setPublshInformation(publishInfo);
        return root;
    }


    private void finalize(SdnList root, ListType listType) {
        root.getPublshInformation()
                .setRecordCount(root.getSdnEntry().size());
        var uuid = new AtomicInteger(0);
        for (var next : root.getSdnEntry()) {
            forEach(next::getAkaList, SdnEntry.AkaList::getAka,
                    e -> e.setCategory("strong")
            );
            forEach(next::getDateOfBirthList, SdnEntry.DateOfBirthList::getDateOfBirthItem,
                    this::formatDOB
            );
            if (ADD_UIDS) {
                forEach(next::getIdList, SdnEntry.IdList::getId,
                        e -> e.setUid(uuid.incrementAndGet())
                );
                forEach(next::getAkaList, SdnEntry.AkaList::getAka,
                        e -> e.setUid(uuid.incrementAndGet())
                );
                forEach(next::getAddressList, SdnEntry.AddressList::getAddress,
                        e -> e.setUid(uuid.incrementAndGet())
                );
                forEach(next::getNationalityList, SdnEntry.NationalityList::getNationality,
                        e -> e.setUid(uuid.incrementAndGet())
                );
                forEach(next::getCitizenshipList, SdnEntry.CitizenshipList::getCitizenship,
                        e -> e.setUid(uuid.incrementAndGet())
                );
                forEach(next::getDateOfBirthList, SdnEntry.DateOfBirthList::getDateOfBirthItem,
                        e -> e.setUid(uuid.incrementAndGet())
                );
                forEach(next::getPlaceOfBirthList, SdnEntry.PlaceOfBirthList::getPlaceOfBirthItem,
                        e -> e.setUid(uuid.incrementAndGet())
                );
            }
            if (listType.equals(ListType.CUSTOM_LIST)) {
                next.setSdnType("Entity");
            }
        }
    }


    private void save(SdnList object, Path outputFile) throws JAXBException {
        var context = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(object, outputFile.toFile());
    }


    private void formatDOB(SdnEntry.DateOfBirthList.DateOfBirthItem item) {
        var dob = item.getDateOfBirth();
        if (StringUtils.isBlank(dob)) {
            // Empty
        } else if (dob.length() == 4) {
            // Assume "YYYY"
        } else {
            var date = IN_FORMATTER.parse(dob);
            item.setDateOfBirth(OUT_FORMATTER.format(date));
        }
    }


    private <T, L> void forEach(Supplier<T> getter,
                                Function<T, List<L>> itemProvider,
                                Consumer<L> consumer) {
        Optional.ofNullable(getter.get())
                .ifPresent(list ->
                        itemProvider.apply(list)
                                .forEach(consumer)
                );
    }

}
