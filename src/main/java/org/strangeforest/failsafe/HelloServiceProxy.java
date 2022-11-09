package org.strangeforest.failsafe;

import dev.failsafe.*;

public class HelloServiceProxy implements HelloService {

   private final HelloService delegate;
   private final Policy<String> policy;

   public HelloServiceProxy(HelloService delegate, Policy<String> policy) {
      this.delegate = delegate;
      this.policy = policy;
   }

   @Override public String hello(String name) {
      return Failsafe.with(policy).get(() -> delegate.hello(name));
   }
}
