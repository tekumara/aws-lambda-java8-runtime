package lambdainternal.api;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import lambdainternal.LambdaRuntime;

public class LambdaContextLogger implements LambdaLogger {
   private static final String NULL_STRING_VALUE = "null";

   public LambdaContextLogger() {
   }

   public void log(byte[] message) {
      if (message == null) {
         message = "null".getBytes();
      }

      LambdaRuntime.sendContextLogs(message, message.length);
   }

   public void log(String message) {
      if (message == null) {
         message = "null";
      }

      byte[] messageBytes = message.getBytes();
      LambdaRuntime.sendContextLogs(messageBytes, messageBytes.length);
   }
}
