package org.strangeforest.failsafe;

import java.time.*;

import dev.failsafe.*;
import org.junit.jupiter.api.*;

import static dev.failsafe.CircuitBreaker.State.*;
import static org.assertj.core.api.Assertions.*;
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

      var hello = new HelloServiceProxy(new HelloServiceImpl(), circuitBreaker);

      assertThat(circuitBreaker.getState()).isEqualTo(CLOSED);

      assertThatThrownBy(() -> hello.hello("Fail")).isInstanceOf(IllegalArgumentException.class);
      assertThat(circuitBreaker.getState()).isEqualTo(CLOSED);

      assertThatThrownBy(() -> hello.hello("Fail")).isInstanceOf(IllegalArgumentException.class);
      assertThat(circuitBreaker.getState()).isEqualTo(CLOSED);

      assertThatThrownBy(() -> hello.hello("Fail")).isInstanceOf(IllegalArgumentException.class);
      assertThat(circuitBreaker.getState()).isEqualTo(OPEN);

      assertThatThrownBy(() -> hello.hello("World")).isInstanceOf(CircuitBreakerOpenException.class);
      assertThat(circuitBreaker.getState()).isEqualTo(OPEN);

      sleep(1);
      assertThat(hello.hello("World")).isEqualTo("Hello World!");
      assertThat(circuitBreaker.getState()).isEqualTo(HALF_OPEN);

      assertThatThrownBy(() -> hello.hello("Fail")).isInstanceOf(IllegalArgumentException.class);
      assertThat(circuitBreaker.getState()).isEqualTo(OPEN);

      assertThatThrownBy(() -> hello.hello("World")).isInstanceOf(CircuitBreakerOpenException.class);
      assertThat(circuitBreaker.getState()).isEqualTo(OPEN);

      sleep(1);
      assertThat(hello.hello("World")).isEqualTo("Hello World!");
      assertThat(circuitBreaker.getState()).isEqualTo(HALF_OPEN);

      assertThat(hello.hello("World")).isEqualTo("Hello World!");
      assertThat(circuitBreaker.getState()).isEqualTo(CLOSED);
   }
}
