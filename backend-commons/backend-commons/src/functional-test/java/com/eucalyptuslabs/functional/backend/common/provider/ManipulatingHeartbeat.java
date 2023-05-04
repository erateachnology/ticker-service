package com.eucalyptuslabs.functional.backend.common.provider;

import java.util.concurrent.atomic.AtomicBoolean;

public record ManipulatingHeartbeat(Runnable heartbeat, AtomicBoolean allowRun)
    implements Runnable {
  public static final String HEARTBEAT_TASK_CLASS_NAME =
      "org.springframework.messaging.simp.broker.SimpleBrokerMessageHandler$HeartbeatTask";

  @Override
  public void run() {
    if (allowRun.get()) {
      heartbeat.run();
    }
  }
}
