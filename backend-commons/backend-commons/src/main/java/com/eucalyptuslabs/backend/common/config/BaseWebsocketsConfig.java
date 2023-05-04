package com.eucalyptuslabs.backend.common.config;

import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@EnableWebSocketMessageBroker
public abstract class BaseWebsocketsConfig implements WebSocketMessageBrokerConfigurer {

  private final String websocketsEndpoint;
  private final long heartbeat;
  private final String origins;

  protected BaseWebsocketsConfig(String websocketsEndpoint, long heartbeat, String origins) {
    this.websocketsEndpoint = websocketsEndpoint;
    this.heartbeat = heartbeat;
    this.origins = origins;
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    if (origins != null && !origins.isEmpty())
      registry.addEndpoint(websocketsEndpoint).setAllowedOrigins(origins);
    else registry.addEndpoint(websocketsEndpoint);
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config
        .enableSimpleBroker("/topic", "/queue")
        .setTaskScheduler(getWebsocketsHeartbeatTaskScheduler())
        .setHeartbeatValue(new long[] {heartbeat, heartbeat});
    config.setApplicationDestinationPrefixes("/endpoint");
    config.configureBrokerChannel().taskExecutor().keepAliveSeconds(1);
  }

  public TaskScheduler getWebsocketsHeartbeatTaskScheduler() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setPoolSize(1);
    taskScheduler.setThreadNamePrefix("websockets-heartbeat-thread-");
    taskScheduler.initialize();
    return taskScheduler;
  }
}
