package lambdainternal;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class UserFault extends RuntimeException {
   private static final long serialVersionUID = 0L;
   public final String msg;
   public final String exception;
   public final String trace;
   public final Boolean fatal;

   public UserFault(String msg, String exception, String trace) {
      this.msg = msg;
      this.exception = exception;
      this.trace = trace;
      this.fatal = false;
   }

   public UserFault(String msg, String exception, String trace, Boolean fatal) {
      this.msg = msg;
      this.exception = exception;
      this.trace = trace;
      this.fatal = fatal;
   }

   public static UserFault makeUserFault(Throwable t) {
      String msg = t.getLocalizedMessage() == null ? t.getClass().getName() : t.getLocalizedMessage();
      return new UserFault(msg, t.getClass().getName(), trace(t));
   }

   public static UserFault makeUserFault(String msg) {
      return new UserFault(msg, (String)null, (String)null);
   }

   public static String trace(Throwable t) {
      filterStackTrace(t);
      StringWriter sw = new StringWriter();
      t.printStackTrace(new PrintWriter(sw));
      return sw.toString();
   }

   public static <T extends Throwable> T filterStackTrace(T t) {
      StackTraceElement[] trace = t.getStackTrace();

      for(int i = 0; i < trace.length; ++i) {
         if (trace[i].getClassName().startsWith("lambdainternal")) {
            StackTraceElement[] newTrace = new StackTraceElement[i];
            System.arraycopy(trace, 0, newTrace, 0, i);
            t.setStackTrace(newTrace);
            break;
         }
      }

      Throwable cause = t.getCause();
      if (cause != null) {
         filterStackTrace(cause);
      }

      return t;
   }
}
