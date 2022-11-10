package org.strangeforest.failsafe;

import java.time.*;

import dev.failsafe.*;
import org.junit.jupiter.api.*;

import static dev.failsafe.CircuitBreaker.State.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.strangeforest.failsafe.Util.*;

class HelloServiceCircuitBreakerTest {

   @Test
   void helloWorld() {
      var circuitBreaker = CircuitBreaker.<String>builder()
         .withFailureThreshold(3)
         .withSuccessThreshold(2)
         .withDelay(Duration.ofSeconds(1L))
         .onOpen(e -> System.out.println("The circuit breaker was opened"))
         .onClose(e -> System.out.println("The circuit breaker was closed"))
         .onHalfOpen(e -> System.out.println("The circuit breaker was half-opened"))
      .build();

      var helloSpy = spy(new HelloServiceImpl());
      var hello = new HelloServiceProxy(helloSpy, circuitBreaker);

      assertThat(circuitBreaker.getState()).isEqualTo(CLOSED);

      assertThatThrownBy(() -> hello.hello("Fail")).isInstanceOf(IllegalArgumentException.class);
      verify(helloSpy, times(1)).hello(anyString());
      assertThat(circuitBreaker.getState()).isEqualTo(CLOSED);

      reset(helloSpy);
      assertThatThrownBy(() -> hello.hello("Fail")).isInstanceOf(IllegalArgumentException.class);
      verify(helloSpy, times(1)).hello(anyString());
      assertThat(circuitBreaker.getState()).isEqualTo(CLOSED);

      reset(helloSpy);
      assertThatThrownBy(() -> hello.hello("Fail")).isInstanceOf(IllegalArgumentException.class);
      verify(helloSpy, times(1)).hello(anyString());
      assertThat(circuitBreaker.getState()).isEqualTo(OPEN);

      reset(helloSpy);
      assertThatThrownBy(() -> hello.hello("World")).isInstanceOf(CircuitBreakerOpenException.class);
      verifyNoInteractions(helloSpy);
      assertThat(circuitBreaker.getState()).isEqualTo(OPEN);

      sleep(1);
      reset(helloSpy);
      assertThat(hello.hello("World")).isEqualTo("Hello World!");
      verify(helloSpy, times(1)).hello(anyString());
      assertThat(circuitBreaker.getState()).isEqualTo(HALF_OPEN);

      reset(helloSpy);
      assertThatThrownBy(() -> hello.hello("Fail")).isInstanceOf(IllegalArgumentException.class);
      verify(helloSpy, times(1)).hello(anyString());
      assertThat(circuitBreaker.getState()).isEqualTo(OPEN);

      reset(helloSpy);
      assertThatThrownBy(() -> hello.hello("World")).isInstanceOf(CircuitBreakerOpenException.class);
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
