package com.integration.backend.ticker.v2;
import com.backend.ticker.TickerBackendApplication;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

@SpringBootTest(properties ={"crypto-rates.providers-cache.refresh.seconds=1"} ,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {TickerBackendApplication.class})
public class TickerApiIntegrationTestV2 extends BaseIntegrationTestV2 {
    @Test
    @DisplayName("Test ticker-complete-rates endpoint")
    public void tickerCompleteRates() throws InterruptedException {
        // Waiting for the cache to be refreshed
        Thread.sleep(1500);
        invokeCompleteRatesUsdEndpoint(localServerPort)
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("expected/complete-rates-response-schema-v2.json"));
    }

    public static ValidatableResponse invokeCompleteRatesUsdEndpoint(int targetServerPort) {
        return invokeCompleteRatesEndpoint(targetServerPort, "USD");
    }

    public static ValidatableResponse invokeCompleteRatesEndpoint(int targetServerPort, String fiat) {
        return RestAssured
                .given()
                .contentType(ContentType.JSON)
                .accept(ContentType.ANY)
                .log().all()
                .when()
                .get(getUrl(targetServerPort, "rates/all/" + fiat))
                .then()
                .log().all();
    }
}
