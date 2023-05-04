package com.eucalyptuslabs.functional.backend.common.provider;

import com.eucalyptuslabs.backend.common.CommonsBackendApplication;
import com.eucalyptuslabs.functional.backend.common.support.BaseMockitoSupport;
import com.eucalyptuslabs.functional.backend.common.support.LogsInterceptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {CommonsBackendApplication.class, LogsInterceptor.class, BaseMockitoSupport.class})
@ActiveProfiles("functional")
@AutoConfigureObservability
public abstract class BaseFunctionalTest {
  protected final Logger log = LoggerFactory.getLogger(getClass());

  @LocalServerPort protected int localServerPort;

  @LocalManagementPort protected int localActuatorPort;

  @Autowired protected LogsInterceptor logsInterceptor;

  @Autowired protected BaseMockitoSupport mockitoSupport;

  @BeforeEach
  public void setUpMocks() throws Exception {
    logsInterceptor.addToIgnoredLoggersByName(getClass().getName());
  }

  @AfterEach
  public void verifyMocks() {
  }
}
