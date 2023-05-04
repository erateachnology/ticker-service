package com.integration.backend.ticker.v2;

import com.backend.ticker.TickerBackendApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.internal.util.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Map;

import static com.eucalyptuslabs.backend.common.util.AssertionUtils.awaitUntilAsserted;
import static com.eucalyptuslabs.integration.backend.common.MetricsIntegrationTest.invokeActuatorHealthEndpoint;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {TickerBackendApplication.class})
@ActiveProfiles("local")
@AutoConfigureObservability
public class BaseIntegrationTestV2 {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @LocalServerPort
    protected int localServerPort;
    @LocalManagementPort
    protected int localActuatorPort;


    @BeforeEach
    public void waitForCacheInit() {
        awaitUntilAsserted(
                30_000,
                1000,
                () -> invokeActuatorHealthEndpoint(localActuatorPort).body("status", equalTo("UP")));
    }

    protected static String getUrl(int targetServerPort, String endpoint) {
        return String.format("http://localhost:%d/ticker/v2/%s", targetServerPort, endpoint);
    }

    public static String loadDto(String path) throws IOException {
        return new String(IOUtils.toByteArray(new ClassPathResource(path).getInputStream()));
    }

    public static JsonNode loadJson(String path) throws IOException {
        return OBJECT_MAPPER.readTree(loadDto(path));
    }

    public static String writeToJson(Map<String, Object> jsonAsMap) throws IOException {
        return OBJECT_MAPPER.writeValueAsString(jsonAsMap);
    }
}
