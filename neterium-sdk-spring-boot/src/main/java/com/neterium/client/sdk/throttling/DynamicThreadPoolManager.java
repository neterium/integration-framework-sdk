package com.neterium.client.sdk.throttling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * A wrapped <code>ThreadPoolTaskExecutor</code>
 * allowing the pool size to be adjusted dynamically based on a {@link Throttler} value
 *
 * @author Bernard Ligny
 */
@Component
@Slf4j
public class DynamicThreadPoolManager {

    private final ThreadPoolTaskExecutor taskExecutor;
    private final Object lock = new Object();


    /**
     * Constructor
     *
     * @param taskExecutor the <code>ThreadPoolTaskExecutor</code> to wrap
     * @param throttler    a {@link Throttler} instance
     */
    public DynamicThreadPoolManager(@Throttleable ThreadPoolTaskExecutor taskExecutor,
                                    Throttler throttler) {
        this.taskExecutor = taskExecutor;
        throttler.addListener(this::updatePoolSize);
    }


    /**
     * Update the core pool size of the underlying <code>ThreadPoolExecutors</code>
     *
     * @see ThrottlerListener#onValueUpdated(String, int, int)
     */
    private void updatePoolSize(String throttlerId, int oldSize, int newSize) {
        assert throttlerId.equals(ThrottlerImpl.class.getName());
        ThreadPoolExecutor executor = taskExecutor.getThreadPoolExecutor();
        try {
            synchronized (lock) {
                executor.setCorePoolSize(newSize);
            }
            var direction = (newSize > oldSize ? "up" : "down");
            log.info("Pool size scaled {} from {} to {}", direction, oldSize, newSize);
        } catch (Throwable t) {
            log.warn("Error while updating pool size", t);
        }
    }

}
