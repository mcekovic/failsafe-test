package org.strangeforest.failsafe;

import java.util.concurrent.*;

import dev.failsafe.*;

public class HelloServiceAsyncProxy {

   private final HelloService delegate;
   private final Policy<String> policy;

   public <T> HelloServiceAsyncProxy(HelloService delegate, Policy<String> policy) {
      this.delegate = delegate;
      this.policy = policy;
   }

   public CompletableFuture<String> hello(String name) {
      return Failsafe.with(policy).getAsync(() -> delegate.hello(name));
   }
}
