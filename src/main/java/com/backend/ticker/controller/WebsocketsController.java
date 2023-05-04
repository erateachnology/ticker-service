package com.backend.ticker.controller;

import com.backend.ticker.models.CurrencyUpdateRequest;
import com.backend.ticker.service.CompleteRatesServiceV1;
import com.backend.ticker.service.CompleteRatesServiceV2;
import com.eucalyptuslabs.backend.common.model.exception.DataNotAvailableException;
import com.eucalyptuslabs.backend.common.model.ticker.response.CompleteRatesResponse;
import com.backend.ticker.config.WebsocketsUpdatesPublisherV1;
import com.backend.ticker.config.WebsocketsUpdatesPublisherV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebsocketsController {
    @Autowired
    private WebsocketsUpdatesPublisherV1 websocketsUpdatesPublisherV1;

    @Autowired
    private WebsocketsUpdatesPublisherV2 websocketsUpdatesPublisherV2;
    @Autowired
    private CompleteRatesServiceV1 completeRatesServiceV1;

    @Autowired
    private CompleteRatesServiceV2 completeRatesServiceV2;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/ticker/v1/rates/update")
    public void tickerUpdateWebsocketsRequestV1(@Payload CurrencyUpdateRequest updateRequest) throws DataNotAvailableException {

        CompleteRatesResponse ratesResponse = completeRatesServiceV1.getCompleteRates(updateRequest.currency());
        websocketsUpdatesPublisherV1.sendToCurrencyTopic(updateRequest.currency(), updateRequest.address(), ratesResponse);
    }

    @MessageMapping("/ticker/v2/rates/update")
    public void tickerUpdateWebsocketsRequestV2(@Payload CurrencyUpdateRequest updateRequest) throws DataNotAvailableException {

        CompleteRatesResponse ratesResponse = completeRatesServiceV2.getCompleteRates(updateRequest.currency());
        websocketsUpdatesPublisherV2.sendToCurrencyTopic(updateRequest.currency(), updateRequest.address(), ratesResponse);
    }
}
