package com.backend.ticker.config;

import com.eucalyptuslabs.backend.common.model.ticker.response.CompleteRatesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebsocketsUpdatesPublisherV1 {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public void sendToCurrencyTopic(String currency, CompleteRatesResponse rates) {
        String destination = "/topic/ticker/rates/" + currency;
        simpMessagingTemplate.convertAndSend(destination, rates);
    }

    public void sendToCurrencyTopic(String currency, String address, CompleteRatesResponse completeRatesService) {
        String destination = String.format(
                "/topic/ticker/rates/%s/%s", currency, address);
        simpMessagingTemplate.convertAndSend(
                destination,
                completeRatesService);
    }
}
