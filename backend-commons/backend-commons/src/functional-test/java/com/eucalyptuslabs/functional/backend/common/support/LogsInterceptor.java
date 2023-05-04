package com.eucalyptuslabs.functional.backend.common.support;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Assertions;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

/** Utility class that allows to intercept Logback logs and make assertions on the log lines. */
@Service
@Scope("prototype")
public class LogsInterceptor implements DisposableBean {

  private static final Set<String> INTERNAL_LOGGER_NAMES =
      Set.of(
          "org.springframework",
          "org.apache.coyote",
          "org.apache.catalina",
          "wiremock.org.eclipse",
          "WireMock");

  private static final Set<String> DEFAULT_IGNORED_LOGGER_NAMES =
      Set.of("com.eucalyptuslabs.backend.common.util.AssertionUtils");

  private final Set<String> ignoredLoggersByName = new HashSet<>();
  private final Logger rootLogger;
  private final ListAppender<ILoggingEvent> listAppender;

  public LogsInterceptor() {

    rootLogger = (Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME);

    listAppender = new ListAppender<>();
    listAppender.start();

    rootLogger.addAppender(listAppender);

    DEFAULT_IGNORED_LOGGER_NAMES.forEach(this::addToIgnoredLoggersByName);
  }

  @Override
  public void destroy() {
    rootLogger.detachAppender(listAppender);
  }

  private Stream<ILoggingEvent> getMessagesStream() {
    return new ArrayList<>(listAppender.list).stream();
  }

  public void addToIgnoredLoggersByName(String loggerName) {
    ignoredLoggersByName.add(loggerName);
  }

  public void removeFromIgnoredLoggersByName(String loggerName) {
    ignoredLoggersByName.remove(loggerName);
  }

  public List<String> getLogMessagesForLogger(String loggerName) {
    return getMessagesStream()
        .filter(e -> e.getLoggerName().equals(loggerName))
        .map(ILoggingEvent::getFormattedMessage)
        .collect(toList());
  }

  public Collection<String> getAndRemoveLogsForBusinessLogic(boolean isOrderRequired) {
    return getAndRemoveLogsForBusinessLogic(null, isOrderRequired);
  }

  public Collection<String> getAndRemoveLogsForBusinessLogic(Level level, boolean orderRequired) {
    List<ILoggingEvent> logs =
        getMessagesStream()
            .filter(
                e -> INTERNAL_LOGGER_NAMES.stream().noneMatch(l -> e.getLoggerName().startsWith(l)))
            .filter(e -> !ignoredLoggersByName.contains(e.getLoggerName()))
            .filter(e -> level == null || e.getLevel().equals(level))
            .toList();
    listAppender.list.removeAll(logs);
    Stream<String> messagesStream = logs.stream().map(ILoggingEvent::getFormattedMessage);
    if (orderRequired) {
      return messagesStream.collect(toList());
    } else {
      return messagesStream.distinct().sorted().collect(Collectors.toList());
    }
  }

  public void assertLogMessagesProduced(Collection<String> infoLogs, Collection<String> debugLogs) {
    assertLogMessagesProduced(infoLogs, debugLogs, emptyList());
  }

  public void assertLogMessagesProduced(
      Collection<String> infoLogs, Collection<String> debugLogs, Collection<String> errorLogs) {
    assertLogMessagesProduced(infoLogs, debugLogs, errorLogs, emptyList());
  }

  public void assertLogMessagesProduced(
      Collection<String> infoLogs,
      Collection<String> debugLogs,
      Collection<String> errorLogs,
      Collection<String> warnLogs) {
    Assertions.assertEquals(
        prepareForComparison(infoLogs),
        getAndRemoveLogsForBusinessLogic(Level.INFO, isOrderRequired(infoLogs)));
    Assertions.assertEquals(
        prepareForComparison(debugLogs),
        getAndRemoveLogsForBusinessLogic(Level.DEBUG, isOrderRequired(debugLogs)));
    Assertions.assertEquals(
        prepareForComparison(errorLogs),
        getAndRemoveLogsForBusinessLogic(Level.ERROR, isOrderRequired(errorLogs)));
    Assertions.assertEquals(
        prepareForComparison(warnLogs),
        getAndRemoveLogsForBusinessLogic(Level.WARN, isOrderRequired(warnLogs)));
    Assertions.assertEquals(emptyList(), getAndRemoveLogsForBusinessLogic(true));
  }

  private Collection<String> prepareForComparison(Collection<String> debugLogs) {
    if (isOrderRequired(debugLogs)) {
      return debugLogs;
    } else {
      return debugLogs.stream().sorted().toList();
    }
  }

  private boolean isOrderRequired(Collection<String> expectedLogs) {
    return expectedLogs instanceof List;
  }

  public void clear() {
    listAppender.list.clear();
  }
}
