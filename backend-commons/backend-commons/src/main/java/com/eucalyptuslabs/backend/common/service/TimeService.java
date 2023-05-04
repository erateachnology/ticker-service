package com.eucalyptuslabs.backend.common.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

/** The common service returning current time and date. */
@Service
public class TimeService {

  public Instant getCurrentInstant() {
    return Instant.now();
  }

  public Date getCurrentDate() {
    return new Date(getCurrentInstant().toEpochMilli());
  }

  public long getCurrentTimestamp() {
    return Instant.now().toEpochMilli();
  }

  public ZonedDateTime getCurrentZonedDateTime() {
    return ZonedDateTime.now(ZoneOffset.UTC);
  }
}
