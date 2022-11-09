package org.strangeforest.failsafe;

import java.io.*;
import java.time.*;

import com.google.common.base.*;
import dev.failsafe.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class HelloServiceRetryTest {

   @Test
   void helloWorld() {
      var retry = traceEvents(RetryPolicy.<String>builder())
         .withMaxRetries(2)
         .build();

      var helloSpy = spy(new HelloServiceImpl());
      var hello = new HelloServiceProxy(helloSpy, retry);

      assertThat(hello.hello("World")).isEqualTo("Hello World!");
      verify(helloSpy, times(1)).hello(anyString());
   }

   @Test
   void helloWorldRetried() {
      var retry = traceEvents(RetryPolicy.<String>builder())
         .withMaxRetries(2)
         .build();

      var helloSpy = spy(new HelloServiceImpl());
      var hello = new HelloServiceProxy(helloSpy, retry);

      assertThat(hello.hello("Fail2")).isEqualTo("Hello Fail2!");
      verify(helloSpy, times(3)).hello(anyString());
   }

   @Test
   void helloWorldRetryFailure() {
      var retry = traceEvents(RetryPolicy.<String>builder())
         .withMaxRetries(2)
         .build();

      var helloSpy = spy(new HelloServiceImpl());
      var hello = new HelloServiceProxy(helloSpy, retry);

      assertThatThrownBy(() -> hello.hello("Fail3"))
         .isInstanceOf(IllegalArgumentException.class);
      verify(helloSpy, times(3)).hello(anyString());
   }

   @Test
   void helloWorldRetriedWithDelay() {
      var retry = traceEvents(RetryPolicy.<String>builder())
         .withMaxRetries(2)
         .withDelay(Duration.ofSeconds(1L))
         .build();

      var stopwatch = Stopwatch.createStarted();
      var helloSpy = spy(new HelloServiceImpl());
      var hello = new HelloServiceProxy(helloSpy, retry);

      assertThat(hello.hello("Fail2")).isEqualTo("Hello Fail2!");
      verify(helloSpy, times(3)).hello(anyString());

      stopwatch.stop();

      assertThat(stopwatch.elapsed()).isGreaterThanOrEqualTo(Duration.ofSeconds(2L));
   }

   @Test
   void helloWorldRetriesAborted() {
      var retry = traceEvents(RetryPolicy.<String>builder())
         .withMaxRetries(2)
         .abortOn(SecurityException.class)
         .build();

      var helloSpy = spy(new HelloServiceImpl());
      var hello = new HelloServiceProxy(helloSpy, retry);

      assertThatThrownBy(() -> hello.hello("Deny"))
         .isInstanceOf(SecurityException.class);

      verify(helloSpy, times(1)).hello(anyString());
   }

   @Test
   void helloWorldRetriesOnSpecificException() {
      var retry = traceEvents(RetryPolicy.<String>builder())
         .withMaxRetries(2)
         .handle(UncheckedIOException.class)
         .build();

      var helloSpy = spy(new HelloServiceImpl());
      var hello = new HelloServiceProxy(helloSpy, retry);

      assertThatThrownBy(() -> hello.hello("IOError"))
         .isInstanceOf(UncheckedIOException.class);
      verify(helloSpy, times(3)).hello(anyString());

      reset(helloSpy);

      assertThatThrownBy(() -> hello.hello("Fail"))
         .isInstanceOf(IllegalArgumentException.class);
      verify(helloSpy, times(1)).hello(anyString());
   }

   private static <R> RetryPolicyBuilder<R> traceEvents(RetryPolicyBuilder<R> retryBuilder) {
      retryBuilder.onSuccess(e -> System.out.println("OnSuccess"));
      retryBuilder.onFailure(e -> System.out.println("OnFailure"));
      retryBuilder.onRetryScheduled(e -> System.out.println("OnRetryScheduled"));
      retryBuilder.onRetry(e -> System.out.println("OnRetry"));
      retryBuilder.onRetriesExceeded(e -> System.out.println("OnRetriesExceeded"));
      retryBuilder.onFailedAttempt(e -> System.out.println("OnFailedAttempt"));
      retryBuilder.onAbort(e -> System.out.println("OnAbort"));
      return retryBuilder;
   }
}
