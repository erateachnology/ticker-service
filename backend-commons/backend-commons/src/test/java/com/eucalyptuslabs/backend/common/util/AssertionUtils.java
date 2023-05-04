package com.eucalyptuslabs.backend.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.internal.util.IOUtils;
import org.awaitility.Awaitility;
import org.awaitility.core.ThrowingRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class AssertionUtils {

  protected static final Logger log = LoggerFactory.getLogger(AssertionUtils.class);

  public static final boolean DEBUG_MODE_DEFAULT = false;
  public static final boolean DEBUG_MODE = getDebugModeProperty(DEBUG_MODE_DEFAULT);
  public static final int SHORTER_POLL_INTERVAL_MILLIS = 50;
  public static final int LONGER_POLL_INTERVAL_MILLIS = 250;
  public static final int POLL_INTERVAL_MILLIS =
      DEBUG_MODE ? LONGER_POLL_INTERVAL_MILLIS : SHORTER_POLL_INTERVAL_MILLIS;

  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper() {
        {
          configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
      };

  private static boolean getDebugModeProperty(boolean developersDefault) {
    String debugModeProperty = System.getenv("DEBUG_MODE");
    if (debugModeProperty == null) {
      return developersDefault;
    }
    return Boolean.parseBoolean(debugModeProperty);
  }

  public static <T> T loadObject(Class<T> objectClass, String dtoPath) throws IOException {
    return loadObject(loadDto(dtoPath), objectClass);
  }

  public static <T> T loadObject(String content, Class<T> objectClass)
      throws JsonProcessingException {
    return OBJECT_MAPPER.readValue(content, objectClass);
  }

  public static <T> T loadObject(JsonNode content, Class<T> objectClass) {
    return OBJECT_MAPPER.convertValue(content, objectClass);
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

  public static void awaitUntilAsserted(
      int timeoutMs, int pollIntervalMillis, ThrowingRunnable throwingRunnable)
      throws InterruptedException {
    Awaitility.await()
        .atMost(timeoutMs, TimeUnit.MILLISECONDS)
        .pollInterval(pollIntervalMillis, TimeUnit.MILLISECONDS)
        .pollDelay(0, TimeUnit.MILLISECONDS)
        .untilAsserted(() -> executeWithDebugLogging(throwingRunnable));
    // allow an asynchronous code to complete before continuing with the other assertions
    Thread.sleep(SHORTER_POLL_INTERVAL_MILLIS);
  }

  public static <V> V awaitUntilAsserted(
      int timeoutMs, int pollIntervalMillis, Callable<V> callable) {
    final AtomicReference<V> returnObject = new AtomicReference<>();
    Awaitility.await()
        .atMost(timeoutMs, TimeUnit.MILLISECONDS)
        .pollInterval(pollIntervalMillis, TimeUnit.MILLISECONDS)
        .pollDelay(0, TimeUnit.MILLISECONDS)
        .untilAsserted(() -> returnObject.set(executeWithDebugLogging(callable)));
    return returnObject.get();
  }

  public static void awaitUntilAsserted(int timeoutMs, ThrowingRunnable throwingRunnable)
      throws InterruptedException {
    awaitUntilAsserted(timeoutMs, POLL_INTERVAL_MILLIS, throwingRunnable);
  }

  public static <V> V awaitUntilAsserted(int timeoutMs, Callable<V> callable) {
    return awaitUntilAsserted(timeoutMs, POLL_INTERVAL_MILLIS, callable);
  }

  public static void awaitUntilAsserted(ThrowingRunnable throwingRunnable)
      throws InterruptedException {
    awaitUntilAsserted(3000, throwingRunnable);
  }

  public static <V> V awaitUntilAsserted(Callable<V> throwingCallable) {
    return awaitUntilAsserted(3000, throwingCallable);
  }

  private static void executeWithDebugLogging(ThrowingRunnable throwingRunnable) throws Throwable {
    try {
      throwingRunnable.run();
    } catch (Throwable e) {
      if (DEBUG_MODE) {
        log.warn("Exception when awaiting on condition: {}", e.getMessage());
      }
      throw e;
    }
  }

  private static <V> V executeWithDebugLogging(Callable<V> throwingRunnable) throws Exception {
    try {
      return throwingRunnable.call();
    } catch (Exception e) {
      if (DEBUG_MODE) {
        log.warn("Exception when awaiting on condition: {}", e.getMessage());
      }
      throw e;
    }
  }
}
