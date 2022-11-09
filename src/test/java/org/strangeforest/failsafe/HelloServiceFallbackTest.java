package org.strangeforest.failsafe;

import dev.failsafe.*;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class HelloServiceFallbackTest {

   @Test
   void helloWorldFallback() {
      var fallback = Fallback.builder("Hello Stranger!")
         .onFailure(e -> System.out.println("Falling back"))
         .build();

      var hello = new HelloServiceProxy(new HelloServiceImpl(), fallback);

      assertThat(hello.hello("World")).isEqualTo("Hello World!");
      assertThat(hello.hello("Fail")).isEqualTo("Hello Stranger!");
   }
}
