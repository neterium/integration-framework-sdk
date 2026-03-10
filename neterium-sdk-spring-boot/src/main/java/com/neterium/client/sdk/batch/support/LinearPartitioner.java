package com.neterium.client.sdk.batch.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamSupport;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Base class to implement a linear <code>Partitioner</code>
 * Creates N partitions depending on
 * - grid size
 * - total numbers of items to process
 * - desired minimum partition size
 *
 * @author Bernard Ligny
 */
@Slf4j
public abstract class LinearPartitioner implements Partitioner {

    /**
     * Key to store the offset when reading data
     */
    public static final String KEY_OFFSET = ".offset";

    /**
     * Key to store the number of items to read
     */
    public static final String KEY_MAX_ITEMS = ".max";

    private final String readerName;
    private Supplier<Integer> itemCountProvider;


    /**
     * Constructor
     *
     * @param readerClass class name of involved reader
     */
    protected LinearPartitioner(Class<? extends ItemStreamSupport> readerClass) {
        this.readerName = readerClass.getSimpleName();
    }


    /**
     * @see Partitioner#partition(int)
     */
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        var offset = getInitialOffset();
        var nbItems = itemCountProvider.get() - offset;
        var rangeSize = Math.max(
                getMinPartitionSize(),
                (int) Math.ceil((double) nbItems / gridSize)
        );
        int nbPartitions = (int) Math.ceil((double) nbItems / rangeSize);
        Map<String, ExecutionContext> partitions = new LinkedHashMap<>(nbPartitions);
        int remainingCount = nbItems;
        for (int i = 0; remainingCount > 0; i++) {
            var maxItems = Math.min(remainingCount, rangeSize);
            Map<String, Object> map = Map.of(
                    readerName + KEY_OFFSET, offset,
                    readerName + KEY_MAX_ITEMS, maxItems
            );
            partitions.put("partition-" + i, new ExecutionContext(map));
            offset = offset + rangeSize;
            remainingCount = remainingCount - rangeSize;
        }
        log.info("{} partition(s) of range {} created for component {}", partitions.size(), rangeSize, readerName);
        log.trace("Partitions ::= {}", partitions);
        return partitions;
    }


    /**
     * Get the initial offset to apply when counting lines
     * (example: a CSV file with a header line)
     */
    protected int getInitialOffset() {
        return 0;
    }


    /**
     * Set the {@link Supplier} to invoke to get the total item count
     */
    protected void setItemCountProvider(Supplier<Integer> itemCountProvider) {
        this.itemCountProvider = itemCountProvider;
    }


    /**
     * Get the minimum of items a partition has to include
     * (none by default)
     */
    protected int getMinPartitionSize() {
        return -1;
    }

}
