package com.neterium.client.sdk.batch.support;

import org.apache.commons.collections4.IteratorUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simple way to partition data using a fair distribution
 *
 * @author Bernard Ligny
 */
public class PartitionKeyFactory {

    /**
     * Default width when not specified
     */
    public static final int DEFAULT_WIDTH = 26;

    private static final char[] RANGE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();

    private final Iterator<String> cyclicIterator;

    /**
     * Create a new factory with default width
     */
    public PartitionKeyFactory() {
        this(DEFAULT_WIDTH);
    }

    /**
     * Create a new factory with provided width (1-26)
     *
     * @param width the desired number of possible keys
     */
    public PartitionKeyFactory(int width) {
        var values = valueRange(width);
        this.cyclicIterator = IteratorUtils.loopingIterator(values);
    }

    /**
     * Given the provided with, compute the range of all possible partition keys
     *
     * @param width the desired number of possible keys
     * @return an exhaustive list of all possible partition keys
     */
    public static List<String> valueRange(int width) {
        var realWidth = Math.min(width, RANGE.length);
        var sequence = new ArrayList<String>(realWidth);
        for (int i = 0; i < realWidth; i++) {
            sequence.add(String.valueOf(RANGE[i]));
        }
        return sequence;
    }

    /**
     * Pick the next value to use to as partition key
     *
     * @return a partition key
     */
    public String pick() {
        return cyclicIterator.next();
    }

}
