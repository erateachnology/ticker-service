package com.eucalyptuslabs.backend.common.filters;

import org.springframework.stereotype.Service;

import java.util.UUID;

/** The implementation of {@link IdGenerator} which produces IDs as {@link UUID}. */
@Service
public class UuidIdGenerator implements IdGenerator {

  public String getNextId() {
    return UUID.randomUUID().toString();
  }
}
