package com.integration.backend.ticker.v2;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.internal.util.IOUtils;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.hamcrest.Matchers.oneOf;
import static org.junit.jupiter.api.Assertions.*;

public class MetricsIntegrationTestV2 extends BaseIntegrationTestV2 {
    public static String metricName = "ticker_websockets_overview";
    @Value("${server.port}")
    private int applicationPort;

    @Value("${management.server.port}")
    private int managementPort;

    @Test // TODO test the logic not the profile
    @DisplayName("Actuator port is different from the main application's port")
    public void shouldExposeActuatorOnDifferentPortTest() {
        assertEquals(0, applicationPort);
        assertEquals(0, managementPort);
        assertNotEquals(localServerPort, localActuatorPort);
    }

    @Test
    @DisplayName("Actuator health endpoint is enabled")
    public void shouldExposeActuatorHealthEndpointOnDifferentPortTest() {

        invokeActuatorHealthEndpoint(localActuatorPort)
                .statusCode(oneOf(200, 503))
                .body("status", oneOf("UP", "OUT_OF_SERVICE"));
    }

    @Test
    @DisplayName("Actuator Prometheus endpoint is enabled")
    public void shouldExposePrometheusEndpoint() {

        invokePrometheusEndpoint(localActuatorPort).statusCode(200);
    }

    private static ValidatableResponse invokeActuatorHealthEndpoint(int targetPort) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.ANY)
                .log()
                .all()
                .when()
                .get(getManagementUrl(targetPort, "actuator/health"))
                .then()
                .log()
                .all();
    }

    public static ValidatableResponse invokePrometheusEndpoint(int targetPort) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.ANY)
                .log()
                .all()
                .when()
                .get(getManagementUrl(targetPort, "actuator/prometheus"))
                .then()
                .log()
                .all();
    }

    @Test
    @DisplayName("Test checking if websockets metrics is present")
    public void checkWebsocketsOverviewMetricsCounterEndpointIsEnabledTest() throws InterruptedException {
        String response = invokePrometheusEndpointWithResponse(localActuatorPort);
        assertTrue(response.contains(metricName + "_total "));
    }

    public static String invokePrometheusEndpointWithResponse(int targetPort) {
        return RestAssured.given()
                .log()
                .all()
                .when()
                .get(getManagementUrl(targetPort, "actuator/prometheus"))
                .body()
                .asString();
    }

    private static String getManagementUrl(int targetServerPort, String endpoint) {
        return String.format("http://localhost:%d/%s", targetServerPort, endpoint);
    }

    public static String loadDto(String path) throws IOException {
        return new String(IOUtils.toByteArray(new ClassPathResource(path).getInputStream()));
    }

    public static String getAuthenticationTokenHeaderValue(String token) {
        return "Bearer " + token;
    }
}
