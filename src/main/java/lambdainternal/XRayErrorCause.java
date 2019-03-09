package lambdainternal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;

public class XRayErrorCause {
   private final String workingDirectory = System.getProperty("user.dir");
   private final Collection<XRayErrorCause.XRayException> exceptions;
   private final Collection<String> paths;

   public XRayErrorCause(Throwable throwable) {
      this.exceptions = Collections.unmodifiableCollection(Collections.singletonList(new XRayErrorCause.XRayException(throwable)));
      this.paths = Collections.unmodifiableCollection((Collection)Arrays.stream(throwable.getStackTrace()).map(StackTraceElement::getFileName).collect(Collectors.toSet()));
   }

   public String getWorkingDirectory() {
      return this.workingDirectory;
   }

   public Collection<XRayErrorCause.XRayException> getExceptions() {
      return this.exceptions;
   }

   public Collection<String> getPaths() {
      return this.paths;
   }

   public static class XRayException {
      private final Throwable throwable;

      public XRayException(Throwable throwable) {
         this.throwable = throwable;
      }

      public String getType() {
         return this.throwable.getClass().getTypeName();
      }

      public String getMessage() {
         return this.throwable.getMessage();
      }

      public StackTraceElement[] getStack() {
         return this.throwable.getStackTrace();
      }
   }
}
