package com.eucalyptuslabs.functional.backend.common.provider;

import com.eucalyptuslabs.functional.backend.common.support.ContextCleaningTestExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestExecutionListeners;

@TestExecutionListeners(
    listeners = ContextCleaningTestExecutionListener.class,
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public abstract class BaseContextCleaningFunctionalTest extends BaseFunctionalTest {
  protected final Logger log = LoggerFactory.getLogger(getClass());
}
