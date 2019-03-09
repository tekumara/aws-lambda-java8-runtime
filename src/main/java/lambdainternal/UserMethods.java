package lambdainternal;

public final class UserMethods {
   public final Runnable initHandler;
   public final LambdaRequestHandler requestHandler;

   public UserMethods(Runnable initHandler, LambdaRequestHandler requestHandler) {
      this.initHandler = initHandler;
      this.requestHandler = requestHandler;
   }
}
