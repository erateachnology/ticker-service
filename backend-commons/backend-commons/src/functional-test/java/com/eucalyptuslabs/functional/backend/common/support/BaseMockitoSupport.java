package com.eucalyptuslabs.functional.backend.common.support;

import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;
import org.springframework.stereotype.Service;

@Service
public class BaseMockitoSupport {

  public void verifyNoMoreInteractions(Object service) {
    if (MockUtil.isMock(service) && !MockUtil.isSpy(service)) {
      Mockito.verifyNoMoreInteractions(service);
    }
  }

  public void clear(Object service) {
    if (MockUtil.isMock(service)) {
      Mockito.clearInvocations(service);
    }
  }
  
  public void verifyNoMoreInteractions() {
  }
  
  public void clearAll() {
  }
}
