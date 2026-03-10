package com.neterium.client.sdk.configuration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Configuration of Spring caching
 *
 * @author Bernard Ligny
 */
@Configuration(proxyBeanMethods = false)
public class CacheConfig {

    /**
     * Name of the cache holding the JWT tokens
     */
    public static final String TOKEN_CACHE = "token-cache";


    /**
     * Constructor
     */
    public CacheConfig() {
    }


    /**
     * Expose a pre-configured <code>CacheManager</code>
     *
     * @return a <code>CacheManager</code> instance
     */
    @Bean
    public CacheManager cacheManager() {
        var cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Set.of(
                new ConcurrentMapCache(TOKEN_CACHE, false)
        ));
        return cacheManager;
    }

}
