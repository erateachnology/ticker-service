package com.eucalyptuslabs.integration.backend.common;

import com.eucalyptuslabs.backend.common.service.WebsocketsClient;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class WebsocketsTestClient extends WebsocketsClient {

  public WebsocketsTestClient(
      TaskScheduler taskScheduler,
      Map<String, Class<?>> topicToHandlerMap,
      AtomicReference<Throwable> failure,
      Long websocketsReconnectWaitTimeMilli,
      Long reconnectTimeoutMilli,
      String url,
      Integer port,
      WebSocketStompClient stompClient) {

    super(
        new TestSessionHandler(topicToHandlerMap, failure),
        taskScheduler,
        url,
        port,
        websocketsReconnectWaitTimeMilli,
        reconnectTimeoutMilli,
        stompClient);
  }

  private static class TestSessionHandler extends WebsocketsClient.MessagesSessionHandler {

    private final AtomicReference<Throwable> failure;

    public TestSessionHandler(
        Map<String, Class<?>> topicToHandlerMap,
        AtomicReference<Throwable> failure) {
      super(topicToHandlerMap);
      this.failure = failure;
    }

    @Override
    public void handleException(
        StompSession s, StompCommand c, StompHeaders h, byte[] p, Throwable ex) {

      super.handleException(s, c, h, p, ex);
      failure.set(ex);
    }

    @Override
    public void handleTransportError(StompSession session, Throwable ex) {
      super.handleTransportError(session, ex);
      failure.set(ex);
    }
  }
}
