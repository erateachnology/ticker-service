package com.eucalyptuslabs.functional.backend.common.support;

import org.mockito.stubbing.Answer;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class DelayedAnswerGenerator {

  public static Answer get(Long delay) {
    return invocation -> {
      Executor executor = CompletableFuture.delayedExecutor(delay, TimeUnit.MILLISECONDS);
      CompletableFuture<StompSession> async =
          CompletableFuture.supplyAsync(
              () -> {
                try {

                  CompletableFuture<StompSession> future =
                      (CompletableFuture<StompSession>) invocation.callRealMethod();
                  return future.get();
                } catch (Throwable e) {
                  throw new RuntimeException(e);
                }
              },
              executor);
      return async;
    };
  }
}
