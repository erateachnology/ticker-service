package com.backend.ticker.service.provider.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class TickerWebsocketsOverviewMetricService {
  private static final String WEBSOCKETS_OVERVIEW_METRIC_NAME = "ticker_websockets_overview";

  AtomicLong currentWebsocketSessionsCount = new AtomicLong(0);

  private final MeterRegistry meterRegistry;

  public TickerWebsocketsOverviewMetricService(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
    Counter.builder(WEBSOCKETS_OVERVIEW_METRIC_NAME)
            .tags(
                    "action",
                    "limit-exceeded-sessions",
                    "action",
                    "http-streaming-sessions",
                    "action",
                    "transport-error-sessions",
                    "action",
                    "total-sessions",
                    "action",
                    "no-message-received-sessions",
                    "action",
                    "http-polling-sessions")
            .register(meterRegistry);
    Gauge.builder(
                    WEBSOCKETS_OVERVIEW_METRIC_NAME + "_current_sessions",
                    currentWebsocketSessionsCount,
                    Number::longValue)
            .description("A custom counter that displays the current number of open websockets")
            .register(meterRegistry);
  }

  public synchronized void increaseMetric(SubProtocolWebSocketHandler.Stats cacheAction) {

    checkAndIncrementMetric(cacheAction.getTransportErrorSessions(), "transport-error-sessions");
    checkAndIncrementMetric(cacheAction.getTotalSessions(), "total-sessions");
    checkAndIncrementMetric(
            cacheAction.getNoMessagesReceivedSessions(), "no-message-received-sessions");
    checkAndIncrementMetric(cacheAction.getHttpPollingSessions(), "http-polling-sessions");
    checkAndIncrementMetric(cacheAction.getHttpStreamingSessions(), "http-streaming-sessions");
    checkAndIncrementMetric(cacheAction.getLimitExceededSessions(), "limit-exceeded-sessions");
    currentWebsocketSessionsCount.set(cacheAction.getWebSocketSessions());

  }

  private void checkAndIncrementMetric(int currentSessionStats, String actionName) {
    int dif = (int) (currentSessionStats - getMetricCount(actionName));
    getMetricCounter(actionName).increment(dif);
  }

  private Counter getMetricCounter(String value) {
    return meterRegistry.counter(WEBSOCKETS_OVERVIEW_METRIC_NAME, "action", value);
  }

  public double getMetricCount(String value) {
    if (value.equals("websocket-sessions")) return currentWebsocketSessionsCount.get();
    return meterRegistry.counter(WEBSOCKETS_OVERVIEW_METRIC_NAME, "action", value).count();
  }

}
