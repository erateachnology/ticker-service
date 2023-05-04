package com.backend.ticker.config;

import com.eucalyptuslabs.backend.common.config.BaseCacheConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TickerCacheConfig extends BaseCacheConfig {

    public static final String COMPLETE_RATES_CACHE_NAME_V1 = "complete-rates-service-v1";

    public static final String COMPLETE_RATES_CACHE_NAME_V2 = "complete-rates-service-v2";

}