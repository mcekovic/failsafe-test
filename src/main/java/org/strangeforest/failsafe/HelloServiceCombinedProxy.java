package org.strangeforest.failsafe;

import java.util.*;

import dev.failsafe.*;

public class HelloServiceCombinedProxy implements HelloService {

   private final HelloService delegate;
   private final Policy<String>[] policies;

   public HelloServiceCombinedProxy(HelloService delegate, Policy<String>... policies) {
      this.delegate = delegate;
      this.policies = policies;
   }

   @Override public String hello(String name) {
      return Failsafe.with(List.of(policies)).get(() -> delegate.hello(name));
   }
}
