package org.strangeforest.failsafe;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

class HelloServiceTest {

   @Test
   void helloWorld() {
      var hello = new HelloServiceImpl();

      var greeting = hello.hello("World");

      assertThat(greeting).isEqualTo("Hello World!");
   }
}
