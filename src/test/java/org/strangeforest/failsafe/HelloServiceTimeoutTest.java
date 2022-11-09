package org.strangeforest.failsafe;

import java.time.*;

import com.google.common.base.*;
import dev.failsafe.Timeout;
import dev.failsafe.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class HelloServiceTimeoutTest {

   @Test
   void helloWorld() {
      var timeout = Timeout.<String>builder(Duration.ofSeconds(2L)).build();

      var hello = new HelloServiceProxy(new HelloServiceImpl(), timeout);

      assertThat(hello.hello("World")).isEqualTo("Hello World!");
      assertThat(hello.hello("Delay1")).isEqualTo("Hello Delay1!");
   }

   @Test
   void helloWorldTimeout() {
      var timeout = Timeout.<String>builder(Duration.ofSeconds(2L)).build();

      var hello = new HelloServiceProxy(new HelloServiceImpl(), timeout);

      var stopwatch = Stopwatch.createStarted();
      assertThatThrownBy(() -> hello.hello("Delay3")).isInstanceOf(TimeoutExceededException.class);
      assertThat(stopwatch.stop().elapsed()).isGreaterThanOrEqualTo(Duration.ofSeconds(3));
   }

   @Test
   void helloWorldTimeoutWithInterrupt() {
      var timeout = Timeout.<String>builder(Duration.ofSeconds(2L)).withInterrupt().build();

      var hello = new HelloServiceProxy(new HelloServiceImpl(), timeout);

      var stopwatch = Stopwatch.createStarted();
      assertThatThrownBy(() -> hello.hello("Delay4")).isInstanceOf(TimeoutExceededException.class);
      assertThat(stopwatch.stop().elapsed()).isLessThan(Duration.ofSeconds(3));
   }
}
