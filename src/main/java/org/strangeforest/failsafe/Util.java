package org.strangeforest.failsafe;

import java.util.concurrent.*;

import dev.failsafe.function.*;

public class Util {

   public static void sleep(int seconds) {
      ignoreExceptions(() -> TimeUnit.SECONDS.sleep(seconds));
   }

   public static void sleepMillis(long millis) {
      ignoreExceptions(() -> TimeUnit.MILLISECONDS.sleep(millis));
   }

   private static void ignoreExceptions(CheckedRunnable action) {
      try {
         action.run();
      }
      catch (Throwable ignored) {}
   }
}
