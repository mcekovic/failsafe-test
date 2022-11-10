package org.strangeforest.failsafe;

import java.io.*;
import java.util.*;

import static org.strangeforest.failsafe.Util.*;

public class HelloServiceImpl implements HelloService {

   private static final String FAIL = "Fail";
   private static final String IO_ERROR = "IOError";
   private static final String DENY = "Deny";
   private static final String DELAY = "Delay";
   private String prevName;
   private int counter;

   @Override public String hello(String name) {
      if (name.startsWith(DELAY)) {
         int delaySeconds = Integer.parseInt(name.substring(DELAY.length()));
         sleep(delaySeconds);
      }
      else if (name.equals(FAIL))
         fail();
      else if (name.startsWith(FAIL)) {
         int failureCount = Integer.parseInt(name.substring(FAIL.length()));
         if (Objects.equals(name, prevName))
            counter++;
         else
            counter = 1;
         prevName = name;
         if (counter % (failureCount + 1) != 0)
            fail();
      }
      else if (name.equals(IO_ERROR))
         ioError();
      else if (name.equals(DENY))
         deny();
      return "Hello %1$s!".formatted(name);
   }

   private static void fail() {
      throw new IllegalArgumentException("Boom!");
   }

   private static void ioError() {
      throw new UncheckedIOException(new IOException("IO Error"));
   }

   private static void deny() {
      throw new SecurityException("Access is denied!");
   }
}
