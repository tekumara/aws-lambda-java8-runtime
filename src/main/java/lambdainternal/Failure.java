package lambdainternal;

import java.io.IOError;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Failure {
   private static final Class[] reportableExceptionsArray = new Class[]{Error.class, ClassNotFoundException.class, IOError.class, Throwable.class, VirtualMachineError.class, LinkageError.class, ExceptionInInitializerError.class, NoClassDefFoundError.class, HandlerInfo.InvalidHandlerException.class};
   private static final List<Class> sortedExceptions;
   private final String errorMessage;
   private final String errorType;
   private final String[] stackTrace;
   private final Failure cause;

   public Failure(Throwable t) {
      this.errorMessage = t.getLocalizedMessage() == null ? t.getClass().getName() : t.getLocalizedMessage();
      this.errorType = t.getClass().getName();
      StackTraceElement[] trace = t.getStackTrace();
      this.stackTrace = new String[trace.length];

      for(int i = 0; i < trace.length; ++i) {
         this.stackTrace[i] = trace[i].toString();
      }

      Throwable cause = t.getCause();
      this.cause = cause == null ? null : new Failure(cause);
   }

   public static Class getReportableExceptionClass(Throwable customerException) {
      return (Class)sortedExceptions.stream().filter((e) -> {
         return e.isAssignableFrom(customerException.getClass());
      }).findFirst().orElse(Throwable.class);
   }

   public static String getReportableExceptionClassName(Throwable f) {
      return getReportableExceptionClass(f).getName();
   }

   public static boolean isInvokeFailureFatal(Throwable t) {
      return t instanceof VirtualMachineError || t instanceof IOError;
   }

   static {
      sortedExceptions = Collections.unmodifiableList((List)Arrays.stream(reportableExceptionsArray).sorted(new Failure.ClassHierarchyComparator()).collect(Collectors.toList()));
   }

   private static class ClassHierarchyComparator implements Comparator<Class> {
      private ClassHierarchyComparator() {
      }

      public int compare(Class o1, Class o2) {
         return o1.isAssignableFrom(o2) ? 1 : -1;
      }
   }
}
