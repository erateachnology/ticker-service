package com.integration.backend.ticker.v1;

import com.eucalyptuslabs.backend.common.model.ticker.response.CompleteRatesResponse;
import com.backend.ticker.TickerBackendApplication;
import com.backend.ticker.service.provider.metrics.TickerWebsocketsOverviewMetricService;
import com.eucalyptuslabs.integration.backend.common.WebsocketsTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static com.eucalyptuslabs.backend.common.util.AssertionUtils.awaitUntilAsserted;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {TickerBackendApplication.class, TickerWebsocketsIntegrationTest.TestConfig.class},
        properties = {
                "crypto-rates.providers-cache.refresh.seconds=5"
        })

public class TickerWebsocketsIntegrationTest extends BaseIntegrationTest {


    @TestConfiguration
    public static class TestConfig {

        @Bean(name = "websockets-client-heartbeat-scheduler")
        public TaskScheduler getWebsocketsHeartbeatTaskScheduler() {
            ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
            taskScheduler.setPoolSize(1);
            taskScheduler.setThreadNamePrefix("websockets-heartbeat-thread-");
            taskScheduler.initialize();
            return taskScheduler;
        }

        @Bean
        public WebSocketStompClient getStompClient(
                @Qualifier("websockets-client-heartbeat-scheduler") TaskScheduler taskScheduler) {
            StandardWebSocketClient client = new StandardWebSocketClient();
            WebSocketStompClient stompClient = new WebSocketStompClient(client);
            stompClient.setTaskScheduler(taskScheduler);
            stompClient.setDefaultHeartbeat(new long[]{5000L, 5000L});
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());
            return stompClient;
        }
    }


    @Autowired
    @Qualifier("websockets-client-heartbeat-scheduler")
    protected TaskScheduler websocketsHeartbeatTaskScheduler;

    @Autowired
    protected WebSocketStompClient stompClient;

    public enum WEBSOCKETS_TEST_ENVIRONMENT {
        LOCAL("ws://localhost:9061/ticker-websockets"),
        TEST("ws://localhost:{port}/ticker-websockets"),
        DEV("wss://backend-dev.euclabs.net/ticker-websockets");

        public final String url;

        WEBSOCKETS_TEST_ENVIRONMENT(String url) {
            this.url = url;
        }
    }

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final String USD_RATES_HEIGHT_TOPIC = "/topic/ticker/rates/USD";
    private static final String EUR_RATES_HEIGHT_TOPIC = "/topic/ticker/rates/EUR";

    AtomicReference<Throwable> failure = new AtomicReference<>();
    private WebsocketsTestClient client;
    @Autowired
    TickerWebsocketsOverviewMetricService tickerWebsocketsOverviewMetricService;

    @BeforeEach
    public void createClient() throws ExecutionException, InterruptedException {
        client =
                new WebsocketsTestClient(
                        websocketsHeartbeatTaskScheduler,
                        Map.of(
                                USD_RATES_HEIGHT_TOPIC, CompleteRatesResponse.class,
                                EUR_RATES_HEIGHT_TOPIC, CompleteRatesResponse.class),
                        failure,
                        15000L,
                        1000L,
                        WEBSOCKETS_TEST_ENVIRONMENT.TEST.url,
                        localServerPort,
                        stompClient);
        client.connect();
    }

    @Test
    public void shouldReceiveTickerRatesUpdates() throws InterruptedException {

        awaitUntilAsserted(40_000, () -> receivedRates(USD_RATES_HEIGHT_TOPIC));
        awaitUntilAsserted(40_000, () -> receivedRates(EUR_RATES_HEIGHT_TOPIC));

        assertNull(failure.get());
        assertTrue(tickerWebsocketsOverviewMetricService.getMetricCount("total-sessions") > 0);
        log.info(
                "Messages from the topic '{}': {}",
                USD_RATES_HEIGHT_TOPIC,
                client.getMessages(CompleteRatesResponse.class, USD_RATES_HEIGHT_TOPIC));
        log.info(
                "Messages from the topic '{}': {}",
                EUR_RATES_HEIGHT_TOPIC,
                client.getMessages(CompleteRatesResponse.class, EUR_RATES_HEIGHT_TOPIC));
    }

    private void receivedRates(String topic) {
        assertTrue(client.getMessages(CompleteRatesResponse.class, topic).stream().findAny().isPresent());
    }
}
