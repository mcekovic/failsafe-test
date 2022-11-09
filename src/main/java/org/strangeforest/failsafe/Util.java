package org.strangeforest.failsafe;

import java.util.concurrent.*;

public class Util {

   public static void sleep(int seconds) {
      try {
         TimeUnit.SECONDS.sleep(seconds);
      }
      catch (InterruptedException ignored) {
      }
   }

   public static void sleepMillis(long millis) {
      try {
         TimeUnit.MILLISECONDS.sleep(millis);
      }
      catch (InterruptedException ignored) {
      }
   }
}
