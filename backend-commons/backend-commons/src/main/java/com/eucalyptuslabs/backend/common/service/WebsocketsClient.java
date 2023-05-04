package com.eucalyptuslabs.backend.common.service;

import com.eucalyptuslabs.backend.common.provider.metrics.WebsocketsCounterMetricService;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WebsocketsClient {

  protected final Logger log = LoggerFactory.getLogger(getClass());
  private final MessagesSessionHandler handler;

  private final WebSocketStompClient stompClient;

  private final String url;
  private final Integer port;
  private final Long connectTimeoutMillis;
  private final TaskScheduler taskScheduler;
  private final Long reconnectIntervalMillis;
  @Getter private volatile boolean isConnected;
  private Optional<WebsocketsCounterMetricService> websocketsCounterMetricService;

  public WebsocketsClient(
      MessagesSessionHandler handler,
      TaskScheduler taskScheduler,
      String url,
      Integer port,
      Long connectTimeoutMillis,
      Long reconnectIntervalMillis,
      WebSocketStompClient stompClient) {
    isConnected = false;
    this.connectTimeoutMillis = connectTimeoutMillis;
    this.stompClient = stompClient;
    this.taskScheduler = taskScheduler;
    this.url = url;
    this.port = port;
    this.reconnectIntervalMillis = reconnectIntervalMillis;
    this.handler = handler;
    this.handler.websocketsClient = this;
    this.websocketsCounterMetricService = Optional.empty();
  }

  public WebsocketsClient(
      MessagesSessionHandler handler,
      TaskScheduler taskScheduler,
      String url,
      Integer port,
      Long connectTimeoutMillis,
      Long reconnectIntervalMillis,
      WebSocketStompClient stompClient,
      WebsocketsCounterMetricService websocketsCounterMetricService) {
    this(
        handler,
        taskScheduler,
        url,
        port,
        connectTimeoutMillis,
        reconnectIntervalMillis,
        stompClient);
    this.websocketsCounterMetricService = Optional.of(websocketsCounterMetricService);
  }

  public void scheduleReconnect() {
    isConnected = false;
    taskScheduler.schedule(this::reconnect, Instant.now().plusMillis(reconnectIntervalMillis));
  }

  private void reconnect() {
    try {
      websocketsCounterMetricService.ifPresent(
          counterMetricService ->
              counterMetricService.increaseMetric(
                  WebsocketsCounterMetricService.WebsocketTags.RECONNECT));
      connect();
    } catch (Exception e) {
      log.debug("Exception when reconnecting websockets", e);
    }
  }

  public void connect() throws ExecutionException, InterruptedException {
    if (isConnected) {
      log.warn("Websockets' connection already established, skipping connection.");
      return;
    }
    log.debug("Attempting to connect to websockets");
    websocketsCounterMetricService.ifPresent(
        counterMetricService ->
            counterMetricService.increaseMetric(
                WebsocketsCounterMetricService.WebsocketTags.TOTAL));
    Object[] uriVariables = Stream.ofNullable(port).toArray();
    CompletableFuture<StompSession> future =
        stompClient.connectAsync(url, new WebSocketHttpHeaders(), handler, uriVariables);
    try {
      future.get(connectTimeoutMillis, TimeUnit.MILLISECONDS);
      log.info("Connection established to websockets.");

    } catch (TimeoutException e) {
      websocketsCounterMetricService.ifPresent(
          counterMetricService ->
              counterMetricService.increaseMetric(
                  WebsocketsCounterMetricService.WebsocketTags.FAIL));

      log.error(
          "Timed out when connecting to websockets, cancelling the current connection attempt.");
      future.cancel(true);
      scheduleReconnect();
    }
  }

  public <T> BlockingQueue<T> getMessages(Class<T> messageClass) {
    return getMessages(messageClass, null);
  }

  public <T> BlockingQueue<T> getMessages(Class<T> messageClass, String topic) {
    List<MessagesStompFrameHandler<T>> matchingHandlers =
        handler.handlerList.stream()
            .filter(h -> h.messageClass.equals(messageClass))
            .filter(h -> topic == null || topic.equals(h.topic))
            .map(h -> (MessagesStompFrameHandler<T>) h)
            .toList();
    if (matchingHandlers.size() > 1) {
      throw new IllegalStateException(
          String.format(
              "too many matching handlers for class %s and topic %s", messageClass, topic));
    }
    return matchingHandlers.stream()
        .findFirst()
        .map(handler -> handler.messages)
        .orElseGet(() -> new ArrayBlockingQueue<>(1));
  }

  public <T> T pollMessage(Class<T> messageClass, String topic) {
    return getMessages(messageClass, topic).poll();
  }

  public static class MessagesStompFrameHandler<V> implements StompFrameHandler {

    public final Class<V> messageClass;
    protected final Logger log = LoggerFactory.getLogger(getClass());
    private final String topic;
    private final BlockingQueue<V> messages = new ArrayBlockingQueue<>(100_000);

    public MessagesStompFrameHandler(String topic, Class<V> messageClass) {
      this.messageClass = messageClass;
      this.topic = topic;
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
      return messageClass;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
      log.debug(
              "Received message on topic {}: {}", topic, payload);
      try {
        messages.put((V) payload);
      } catch (InterruptedException e) {
        Thread.interrupted();
        throw new RuntimeException(e);
      }
    }
  }

  public static class MessagesSessionHandler extends StompSessionHandlerAdapter {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final Map<String, Class<?>> topicToHandlerMap;
    private WebsocketsClient websocketsClient;
    private volatile List<MessagesStompFrameHandler<?>> handlerList = List.of();

    public MessagesSessionHandler(Map<String, Class<?>> transactionsTopic) {
      this.topicToHandlerMap = transactionsTopic;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {}

    @Override
    public void afterConnected(final StompSession session, StompHeaders connectedHeaders) {
      if (handlerList.isEmpty()) {
        handlerList =
            topicToHandlerMap.entrySet().stream()
                .map(e -> new MessagesStompFrameHandler<>(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
      }
      handlerList.forEach(handler -> session.subscribe(handler.topic, handler));
      websocketsClient.isConnected = true;
      websocketsClient.websocketsCounterMetricService.ifPresent(
          counterMetricService ->
              counterMetricService.increaseMetric(
                  WebsocketsCounterMetricService.WebsocketTags.SUCCESS));
      log.debug("connected to session {}", session.getSessionId());
    }

    @Override
    public void handleException(
        StompSession session,
        StompCommand command,
        StompHeaders headers,
        byte[] payload,
        Throwable exception) {
      websocketsClient.websocketsCounterMetricService.ifPresent(
          counterMetricService ->
              counterMetricService.increaseMetric(
                  WebsocketsCounterMetricService.WebsocketTags.FAIL));

      log.error(
          String.format("Handling Websockets exception for session %s", session.getSessionId()),
          exception);
    }

    @Override
    public void handleTransportError(StompSession session, Throwable ex) {
      log.error("Handling transport error for session " + session.getSessionId(), ex);
      websocketsClient.websocketsCounterMetricService.ifPresent(
          counterMetricService ->
              counterMetricService.increaseMetric(
                  WebsocketsCounterMetricService.WebsocketTags.FAIL));

      websocketsClient.scheduleReconnect();
    }
  }
}
