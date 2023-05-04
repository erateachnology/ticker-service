package com.eucalyptuslabs.functional.backend.common.support;

import org.springframework.core.Ordered;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractDirtiesContextTestExecutionListener;

public class ContextCleaningTestExecutionListener
    extends AbstractDirtiesContextTestExecutionListener {

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  @Override
  public void afterTestMethod(TestContext testContext) {
    dirtyContext(testContext, DirtiesContext.HierarchyMode.EXHAUSTIVE);
  }

  @Override
  public void afterTestClass(TestContext testContext) {
    dirtyContext(testContext, DirtiesContext.HierarchyMode.EXHAUSTIVE);
  }
}
