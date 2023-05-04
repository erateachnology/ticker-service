package com.backend.ticker.error;

import com.backend.ticker.MockitoSupport;
import com.eucalyptuslabs.backend.common.service.TimeService;
import com.backend.ticker.service.CompleteRatesServiceV1;
import com.backend.ticker.service.provider.IndependentProvidersCacheScheduler;
import com.eucalyptuslabs.functional.backend.common.provider.BaseFunctionalTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.util.List;

import static com.eucalyptuslabs.backend.common.util.AssertionUtils.loadJson;
import static com.integration.backend.ticker.v1.TickerApiIntegrationTest.invokeCompleteRatesEndpoint;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EndpointErrorHandlingFunctionalTest extends BaseFunctionalTest {


    @MockBean
    private TimeService timeService;
    @MockBean
    private IndependentProvidersCacheScheduler providerCacheScheduler;
    @Autowired
    private MockitoSupport mockitoSupport;
    @Autowired
    private CompleteRatesServiceV1 completeRatesServiceV1;


    public EndpointErrorHandlingFunctionalTest() {
    }

    @BeforeEach
    public void setUpMocks() throws Exception {
        super.setUpMocks();
        mockitoSupport.mockIdGeneratorGetNextId();
        mockitoSupport.mockTimeServiceGetZonedDateTime();
    }

    @AfterEach
    public void verifyMocks() {
        mockitoSupport.verifyNoMoreInteractions();
        super.verifyMocks();
    }

    @Test
    @DisplayName("Should handle cache failure as service unavailable error response")
    public void handleCacheFailureAsServiceUnavailableFunctionalityTest() throws IOException {

        JsonNode actualError =
                invokeCompleteRatesEndpoint(localServerPort, "USD")
                        .statusCode(500)
                        .extract()
                        .as(JsonNode.class);

        assertEquals(
                loadJson("dto/expected/error/service-unavailable-error-response.json"), actualError);

        logsInterceptor.assertLogMessagesProduced(
                emptyList(),
                List.of(),
                List.of(
                ),
                List.of(
                        "Error ID=00000000-0000-0064-0000-0000000000c8 for request method=GET, URI=/ticker/v1/rates/complete/fiat/USD, params=null, body=<no request body>, headers=[content-type, accept, host, connection, user-agent, accept-encoding], responseCode=500, internalCode=HTTP-500, message=null, body=null"));
    }
}
