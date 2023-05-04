package com.eucalyptuslabs.backend.common.filters;

import org.springframework.stereotype.Service;

/** The generic interface for ID producing classes. */
@Service
public interface IdGenerator {

  String getNextId();
}
