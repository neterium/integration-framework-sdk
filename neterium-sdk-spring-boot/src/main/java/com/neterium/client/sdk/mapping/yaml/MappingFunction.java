package com.neterium.client.sdk.mapping.yaml;

import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Possible functions that can be applied to N source values of a {@link MappingEntry}
 * in order to compute a single target value
 *
 * @author Bernard Ligny
 */
public enum MappingFunction {

    /**
     * Function that returns the first non-empty argument value
     */
    FIRST(MappingFunction::firstAvailable, true),

    /**
     * Function that returns the concatenation of all argument values
     */
    CONCAT(MappingFunction::concat, true),

    /**
     * Function that parse an amount in SWIFT FIN MT messages
     */
    MT_AMOUNT(MappingFunction::mtAmount, true),

    /**
     * Function that returns a list of all argument values
     */
    COLLECT(MappingFunction::collect, true),

    /**
     * Function that returns a literal
     */
    CONSTANT(MappingFunction::constant, false) {
    },

    /**
     * Function that returns the position of the first non-empty argument value
     */
    LOOKUP(MappingFunction::lookup, true) {
    };


    private final Function<List<?>, Object> function;

    @Getter
    private final boolean dynamic;

    /**
     * Constructor
     *
     * @param function the java function
     * @param dynamic  whether function arguments are literals (false) or dynamic values (true)
     */
    MappingFunction(Function<List<?>, Object> function, boolean dynamic) {
        this.function = function;
        this.dynamic = dynamic;
    }

    private static Object firstAvailable(List<?> values) {
        return values.stream()
                .filter(v -> !Objects.isNull(v))
                .findFirst()
                .orElse(null);
    }

    private static Object concat(List<?> values) {
        return values.stream()
                .filter(Objects::nonNull)
                .map(Objects::toString)
                .collect(Collectors.joining(", "));
    }

    private static Object collect(List<?> values) {
        return values.stream()
                .filter(v -> !Objects.isNull(v))
                .toList();
    }

    private static Object constant(List<?> values) {
        return values.stream()
                .findFirst()
                .orElse(null);
    }

    private static Object mtAmount(List<?> values) {
        return values.stream()
                .filter(v -> !Objects.isNull(v))
                .findFirst()
                .map(v -> v.toString().replace(',', '.'))
                .orElse(null);
    }

    private static Integer lookup(List<?> values) {
        return IntStream.range(0, values.size())
                .filter(streamIndex ->!Objects.isNull(values.get(streamIndex)))
                .findFirst()
                .orElse(-1);
    }

    /**
     * Executes this function with the provided arguments
     *
     * @param arguments values of the function arguments
     * @param clazz     the class used to cast function result
     * @param <T>       type of provided class
     * @return a result of T type
     */
    public <T> T apply(List<?> arguments, Class<T> clazz) {
        return clazz.cast(function.apply(arguments));
    }

}
