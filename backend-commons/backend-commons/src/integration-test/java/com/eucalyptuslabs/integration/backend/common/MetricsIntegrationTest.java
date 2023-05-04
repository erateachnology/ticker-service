package com.eucalyptuslabs.integration.backend.common;

import com.eucalyptuslabs.backend.common.CommonsBackendApplication;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.internal.util.IOUtils;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;

import static org.hamcrest.Matchers.oneOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {CommonsBackendApplication.class})
@AutoConfigureObservability
@ActiveProfiles("local")
public class MetricsIntegrationTest {

  @LocalServerPort private int localServerPort;

  @LocalManagementPort private int localActuatorPort;

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
        .body("errorCode", oneOf("UP", "OUT_OF_SERVICE"));
  }

  @Test
  @DisplayName("Actuator Prometheus endpoint is enabled")
  public void shouldExposePrometheusEndpoint() {

    invokePrometheusEndpoint(localActuatorPort).statusCode(200);
  }

  public static ValidatableResponse invokeActuatorHealthEndpoint(int targetPort) {
    return RestAssured.given()
        .contentType(ContentType.JSON)
        .accept(ContentType.ANY)
        .log()
        .all()
        .when()
        .get(getUrl(targetPort, "actuator/health"))
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
        .get(getUrl(targetPort, "actuator/prometheus"))
        .then()
        .log()
        .all();
  }

  public static String getUrl(int targetServerPort, String endpoint) {
    return String.format("http://localhost:%d/%s", targetServerPort, endpoint);
  }

  public static String loadDto(String path) throws IOException {
    return new String(IOUtils.toByteArray(new ClassPathResource(path).getInputStream()));
  }

  public static String getAuthenticationTokenHeaderValue(String token) {
    return "Bearer " + token;
  }
}
