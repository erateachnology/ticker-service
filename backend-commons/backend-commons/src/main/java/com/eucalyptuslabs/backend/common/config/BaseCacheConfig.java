package com.eucalyptuslabs.backend.common.config;

import com.google.common.cache.CacheBuilder;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentMap;

/**
 * Base class for cache configuration. Create a class inheriting from this one and annotate it with
 * {@link Configuration} to enable custom cache configuration. Override the method {@link
 * BaseCacheConfig#getConcurrentCacheMap()} for cache customization (e.g. adding eviction timeout).
 */
public class BaseCacheConfig implements CachingConfigurer {

  @Override
  public CacheManager cacheManager() {
    return new ConcurrentMapCacheManager() {
      @Override
      protected Cache createConcurrentMapCache(final String name) {
        ConcurrentMap<Object, Object> store = getConcurrentCacheMap();
        return new ConcurrentMapCache(name, store, true);
      }
    };
  }

  protected ConcurrentMap<Object, Object> getConcurrentCacheMap() {
    return CacheBuilder.newBuilder().build().asMap();
  }
}
