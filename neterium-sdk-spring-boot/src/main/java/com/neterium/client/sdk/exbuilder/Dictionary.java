package com.neterium.client.sdk.exbuilder;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Dictionary
 *
 * @author Bernard Ligny
 */
@Component
@Slf4j
public class Dictionary {

    private static final int FUNCTIONS_CATEGORY = 1;
    private static final int OPERATORS_CATEGORY = 2;
    private static final int DATA_PATHS_CATEGORY = 3;

    private final ObjectMapper objectMapper;
    private Map<Integer, List<Entry>> entries;


    public Dictionary(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @PostConstruct
    private void load() {
        try (var is = getClass().getClassLoader().getResourceAsStream("exceptions.json")) {
            MappingIterator<Entry> it = objectMapper.readerFor(Entry.class).readValues(is);
            entries = it.readAll()
                    .stream()
                    .collect(Collectors.groupingBy(Entry::getCategoryId));
        } catch (Exception e) {
            log.error("Error while loading data", e);
        }

    }


    public List<FunctionDefinition> getFunctions(boolean includeDraft) {
        return entries.get(FUNCTIONS_CATEGORY)
                .stream()
                .filter(this.withDraft(includeDraft))
                .map(entry -> {
                    var counter = new AtomicInteger();
                    return entry.getSyntaxes()
                            .stream()
                            .map(s -> new FunctionDefinition(entry, s, counter.incrementAndGet()))
                            .toList();
                })
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(Displayable::getLabel))
                .toList();
    }

    public FunctionDefinition getFunction(String id) {
        return getFunctions(true)
                .stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }


    public List<OperatorDefinition> getOperators(boolean includeDraft) {
        return entries.get(OPERATORS_CATEGORY)
                .stream()
                .filter(this.withDraft(includeDraft))
                .map(OperatorDefinition::new)
                .toList();
    }

    public OperatorDefinition getOperator(String id) {
        return getOperators(true)
                .stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }


    public List<PathDefinition> getDataPaths(boolean includeDraft) {
        return entries.get(DATA_PATHS_CATEGORY)
                .stream()
                .filter(this.withDraft(includeDraft))
                .map(entry -> entry.getSyntaxes()
                        .stream()
                        .map(s -> new PathDefinition(entry, s))
                        .sorted(Comparator.comparing(Displayable::getLabel))
                        .toList()
                ).flatMap(Collection::stream)
                .toList();
    }

    public PathDefinition getDataPath(String id) {
        return getDataPaths(true)
                .stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private Predicate<Entry> withDraft(boolean includeDraft) {
        return (e -> includeDraft || "published".equalsIgnoreCase(e.getStatus()));
    }

}
