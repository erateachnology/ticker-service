package com.backend.ticker.service.provider.rates.crypto.KdSwap;

import com.eucalyptuslabs.backend.common.provider.BaseProvidersCacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class KdSwapRatesCacheService extends
        BaseProvidersCacheService<Map<String, String>> {

    private static final String DATA_NAME = "KDSwapRatesCache";

    @Value("${crypto-rates.providers-cache.refresh.seconds}")
    private int configRefreshIntervalSeconds;

    @Value("${crypto-rates.providers-cache.enabled:true}")
    private boolean enabled;

    public KdSwapRatesCacheService(KdSwapProviderService kdSwapRatesProviderService) {
        super(DATA_NAME, List.of(kdSwapRatesProviderService));
    }

    @Override
    public int getRefreshIntervalSeconds() {
        return configRefreshIntervalSeconds;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
