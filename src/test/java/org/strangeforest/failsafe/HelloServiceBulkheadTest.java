package org.strangeforest.failsafe;

import java.time.*;
import java.util.concurrent.*;
import java.util.stream.*;

import com.google.common.base.*;
import dev.failsafe.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.strangeforest.failsafe.Util.*;

class HelloServiceBulkheadTest {

   @Test
   void helloWorldConcurrently() throws InterruptedException {
      int maxConcurrency = 5;
      var bulkhead = Bulkhead.<String>builder(maxConcurrency).build();

      var hello = new HelloServiceProxy(new HelloServiceImpl(), bulkhead);

      var executor = Executors.newCachedThreadPool();
      var counter = new CountDownLatch(maxConcurrency);
      for (int i = 0; i < maxConcurrency; i++) {
         executor.execute(() -> {
            counter.countDown();
            assertThat(hello.hello("Delay1")).isEqualTo("Hello Delay1!");
         });
      }
      counter.await();
      sleepMillis(100L);
      assertThatThrownBy(() -> hello.hello("Delay1")).isInstanceOf(BulkheadFullException.class);

      sleep(1);

      assertThat(hello.hello("World")).isEqualTo("Hello World!");
   }

   @Test
   void helloWorldConcurrentlyAsync() throws ExecutionException, InterruptedException {
      int maxConcurrency = 5;
      var bulkhead = Bulkhead.<String>builder(maxConcurrency)
         .withMaxWaitTime(Duration.ofSeconds(2L))
         .build();

      var hello = new HelloServiceAsyncProxy(new HelloServiceImpl(), bulkhead);

      var stopwatch = Stopwatch.createStarted();
      var results = IntStream.range(0, maxConcurrency + 1)
         .mapToObj(i -> hello.hello("Delay1"))
         .toList();

      for (var result : results)
         assertThat(result.get()).isEqualTo("Hello Delay1!");
      assertThat(stopwatch.stop().elapsed()).isGreaterThanOrEqualTo(Duration.ofSeconds(2L));

      stopwatch.reset().start();
      var passedResult = hello.hello("World");

      assertThat(passedResult.get()).isEqualTo("Hello World!");
      assertThat(stopwatch.stop().elapsed()).isLessThan(Duration.ofSeconds(2L));
   }
}
