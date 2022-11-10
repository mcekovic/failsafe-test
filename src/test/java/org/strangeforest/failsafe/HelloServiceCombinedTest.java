package org.strangeforest.failsafe;

import java.time.*;

import dev.failsafe.*;
import org.junit.jupiter.api.*;

import static dev.failsafe.CircuitBreaker.State.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.strangeforest.failsafe.Util.*;

class HelloServiceCombinedTest {

   @Test
   void helloWorld() {
      var retry = RetryPolicy.<String>builder()
         .withMaxRetries(1)
         .build();

      var circuitBreaker = CircuitBreaker.<String>builder()
         .withFailureThreshold(3)
         .withSuccessThreshold(2)
         .withDelay(Duration.ofSeconds(1L))
         .build();

      var helloSpy = spy(new HelloServiceImpl());
      var hello = new HelloServiceCombinedProxy(helloSpy, retry, circuitBreaker);

      assertThat(hello.hello("World")).isEqualTo("Hello World!");
      verify(helloSpy, times(1)).hello(anyString());
      assertThat(circuitBreaker.getState()).isEqualTo(CLOSED);

      reset(helloSpy);
      assertThat(hello.hello("Fail1")).isEqualTo("Hello Fail1!");
      verify(helloSpy, times(2)).hello(anyString());
      assertThat(circuitBreaker.getState()).isEqualTo(CLOSED);

      reset(helloSpy);
      assertThatThrownBy(() -> hello.hello("Fail2"))
         .isInstanceOf(IllegalArgumentException.class);
      verify(helloSpy, times(2)).hello(anyString());
      assertThat(circuitBreaker.getState()).isEqualTo(CLOSED);

      reset(helloSpy);
      assertThatThrownBy(() -> hello.hello("Fail3"))
         .isInstanceOf(CircuitBreakerOpenException.class);
      verify(helloSpy, times(1)).hello(anyString());
      assertThat(circuitBreaker.getState()).isEqualTo(OPEN);

      reset(helloSpy);
      assertThatThrownBy(() -> hello.hello("World"))
         .isInstanceOf(CircuitBreakerOpenException.class);
      verifyNoInteractions(helloSpy);
      assertThat(circuitBreaker.getState()).isEqualTo(OPEN);

      sleep(1);

      reset(helloSpy);
      assertThat(hello.hello("World")).isEqualTo("Hello World!");
      verify(helloSpy, times(1)).hello(anyString());
      assertThat(circuitBreaker.getState()).isEqualTo(HALF_OPEN);

      reset(helloSpy);
      assertThat(hello.hello("World")).isEqualTo("Hello World!");
      verify(helloSpy, times(1)).hello(anyString());
      assertThat(circuitBreaker.getState()).isEqualTo(CLOSED);
   }
}
