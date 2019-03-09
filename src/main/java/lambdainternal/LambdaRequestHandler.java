package lambdainternal;

import lambdainternal.util.LambdaByteArrayOutputStream;

public interface LambdaRequestHandler {
   LambdaByteArrayOutputStream call(LambdaRuntime.InvokeRequest var1) throws Error, Exception;

   public static class UserFaultHandler implements LambdaRequestHandler {
      private final UserFault fault;

      public UserFaultHandler(UserFault fault) {
         this.fault = fault;
      }

      public LambdaByteArrayOutputStream call(LambdaRuntime.InvokeRequest request) {
         throw this.fault;
      }
   }
}
