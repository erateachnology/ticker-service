package com.eucalyptuslabs.backend.common.provider.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class WebsocketsCounterMetricService {
  // available actions: success, fail, reconnect, total
  public static final String WEBSOCKETS_METRIC_NAME = "websockets_client_count_";

  private final MeterRegistry meterRegistry;

  public enum WebsocketTags {
    SUCCESS,
    FAIL,
    RECONNECT,
    TOTAL
  }

  public WebsocketsCounterMetricService(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;

    Arrays.stream(WebsocketTags.values())
        //     .map(tag -> tag.toString().toLowerCase())
        .forEach(
            tag -> {
              Counter.builder(WEBSOCKETS_METRIC_NAME + (tag.name().toLowerCase()))
                  .description(
                      String.format(
                          "A websockets counter that displays the number of %s attempts for websockets connections",
                          tag.name().toLowerCase()))
                  .register(meterRegistry);
              getMetric(tag);
            });
  }

  public void increaseMetric(WebsocketTags cacheAction) {
    getMetric(cacheAction).increment();
  }

  public Counter getMetric(WebsocketTags cacheAction) {
    return getMetricCounter(cacheAction.name().toLowerCase());
  }

  private Counter getMetricCounter(String action) {
    return meterRegistry.counter(WEBSOCKETS_METRIC_NAME + action);
  }
}
