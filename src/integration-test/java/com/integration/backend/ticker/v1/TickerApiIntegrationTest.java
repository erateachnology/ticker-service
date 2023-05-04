package com.integration.backend.ticker.v1;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class TickerApiIntegrationTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Test ticker-complete-rates endpoint")
    public void tickerCompleteRates() {

        invokeCompleteRatesUsdEndpoint(localServerPort)
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("expected/complete-rates-response-schema-v1.json"));
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
                .get(getUrl(targetServerPort, "rates/complete/fiat/" + fiat))
                .then()
                .log().all();
    }


}
