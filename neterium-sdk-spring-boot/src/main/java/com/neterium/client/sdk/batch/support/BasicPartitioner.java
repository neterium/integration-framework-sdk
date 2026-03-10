package com.neterium.client.sdk.batch.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamSupport;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A basic <code>Partitioner</code> implementation that is creating
 * as many partitions as specified width.
 * Each partition will be associated to a partition key
 *
 * @author Bernard Ligny
 * @see PartitionKeyFactory
 */
@Slf4j
public class BasicPartitioner implements Partitioner {

    /**
     * Suffix of the partition-oriented properties stored in SpringBatch <code>ExecutionContext</code>
     */
    public static final String KEY_PARTITION_KEY = ".part-key";

    private final String readerName;
    private final int width;


    /**
     * Create a BasicPartitioner of default width (26)
     *
     * @param readerClass class name of involved reader
     */
    public BasicPartitioner(Class<? extends ItemStreamSupport> readerClass) {
        this(readerClass, 26);
    }

    /**
     * Create a BasicPartitioner of specific width
     *
     * @param readerClass class name of involved reader
     * @param width       desired partition size (1-26)
     */
    public BasicPartitioner(Class<? extends ItemStreamSupport> readerClass,
                            int width) {
        this.readerName = readerClass.getSimpleName();
        this.width = width;
    }


    /**
     * @see Partitioner#partition(int)
     */
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        var values = PartitionKeyFactory.valueRange(width);
        Map<String, ExecutionContext> partitions = new LinkedHashMap<>();
        for (int i = 0; i < values.size(); i++) {
            Map<String, Object> map = Map.of(
                    readerName + KEY_PARTITION_KEY, values.get(i)
            );
            partitions.put("partition-" + i, new ExecutionContext(map));
        }
        log.info("{} partition(s) created for component {}", partitions.size(), readerName);
        log.trace("Partitions ::= {}", partitions);
        return partitions;
    }

}
