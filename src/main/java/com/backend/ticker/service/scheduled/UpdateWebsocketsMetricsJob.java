package com.backend.ticker.service.scheduled;

import com.backend.ticker.service.provider.metrics.TickerWebsocketsOverviewMetricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;

@Service
public class UpdateWebsocketsMetricsJob {

    @Autowired
    private WebSocketMessageBrokerStats webSocketMessageBrokerStats;
    @Autowired
    private TickerWebsocketsOverviewMetricService websocketsOverviewMetricService;
    @Autowired
    private SubProtocolWebSocketHandler subProtocolWebSocketHandler;

    @Scheduled(fixedDelay = 1000, initialDelay = 1000)
    public void updateWebsocketsMetrics() {
        websocketsOverviewMetricService.increaseMetric(subProtocolWebSocketHandler.getStats());
    }


}
