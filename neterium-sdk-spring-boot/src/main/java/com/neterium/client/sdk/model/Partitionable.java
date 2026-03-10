package com.neterium.client.sdk.model;

/**
 * Ability of being partitioned when processing large volumes(for instance with SpringBatch).
 * Each instance will be associated to a partition key (eg a hash)
 * so that instances may be grouped by partition key.
 *
 * @author Bernard Ligny
 */
public interface Partitionable {

    /**
     * Get partition key
     *
     * @return value of the partition key
     */
    String getPartitionKey();

    /**
     * Set partition key
     *
     * @param key the partition key value
     */
    void setPartitionKey(String key);

}
