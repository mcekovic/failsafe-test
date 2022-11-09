package org.strangeforest.failsafe;

import java.time.*;

import dev.failsafe.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.failsafe.Util.*;

class HelloServiceRateLimiterTest {

   @Test
   void helloWorldBurst() {
      int maxExecutions = 5;
      var period = Duration.ofSeconds(1);
      var rateLimiter = RateLimiter.<String>burstyBuilder(maxExecutions, period).build();

      var hello = new HelloServiceProxy(new HelloServiceImpl(), rateLimiter);

      for (int i = 0; i < maxExecutions; i++)
         assertThat(hello.hello("World")).isEqualTo("Hello World!");

      assertThatThrownBy(() -> hello.hello("World")).isInstanceOf(RateLimitExceededException.class);

      sleep(1);

      for (int i = 0; i < maxExecutions + 1; i++) {
         sleepMillis(period.toMillis() / maxExecutions + 1);
         assertThat(hello.hello("World")).isEqualTo("Hello World!");
      }
   }
}
