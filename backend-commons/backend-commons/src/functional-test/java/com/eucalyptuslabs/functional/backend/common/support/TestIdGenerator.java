package com.eucalyptuslabs.functional.backend.common.support;

import com.eucalyptuslabs.backend.common.filters.IdGenerator;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * Test implementation of {@link com.eucalyptuslabs.backend.common.filters.IdGenerator}. It returns
 * readable, sequential IDs.
 */
@Service
@Primary
@Scope(SCOPE_PROTOTYPE)
public class TestIdGenerator implements IdGenerator {

  private final AtomicInteger idCounter = new AtomicInteger(0);

  public String getNextId() {
    return String.format("AutoTestId-%d", idCounter.incrementAndGet());
  }
}
