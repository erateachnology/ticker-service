package com.backend.ticker.config;

import com.eucalyptuslabs.backend.common.config.BaseWebsocketsConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class WebsocketsConfig extends BaseWebsocketsConfig {
    protected WebsocketsConfig() {
        super("/ticker-websockets", 5000L, "*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config
                .enableSimpleBroker("/topic", "/queue")
                .setTaskScheduler(getWebsocketsHeartbeatTaskScheduler())
                .setHeartbeatValue(new long[]{5000L, 5000L});
        config.setApplicationDestinationPrefixes("/endpoint");
        config.configureBrokerChannel().taskExecutor().keepAliveSeconds(1);
    }

    @Bean
    public TaskScheduler getWebsocketsHeartbeatTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setThreadNamePrefix("websockets-heartbeat-thread-");
        taskScheduler.initialize();
        return taskScheduler;
    }
}
