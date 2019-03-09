package lambdainternal.api;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.LambdaRuntime;

public class LambdaContext implements Context {
   private int memoryLimit;
   private final String awsRequestId;
   private final String logGroupName;
   private final String logStreamName;
   private final String functionName;
   private final String functionVersion;
   private final String invokedFunctionArn;
   private final CognitoIdentity cognitoIdentity;
   private final ClientContext clientContext;
   private final LambdaLogger logger;

   public LambdaContext(int memoryLimit, String requestId, String logGroupName, String logStreamName, String functionName, CognitoIdentity identity, String functionVersion, String invokedFunctionArn, ClientContext clientContext) {
      this.memoryLimit = memoryLimit;
      this.awsRequestId = requestId;
      this.logGroupName = logGroupName;
      this.logStreamName = logStreamName;
      this.functionName = functionName;
      this.cognitoIdentity = identity;
      this.clientContext = clientContext;
      this.functionVersion = functionVersion;
      this.invokedFunctionArn = invokedFunctionArn;
      this.logger = LambdaRuntime.getLogger();
   }

   public int getMemoryLimitInMB() {
      return this.memoryLimit;
   }

   public String getAwsRequestId() {
      return this.awsRequestId;
   }

   public String getLogGroupName() {
      return this.logGroupName;
   }

   public String getLogStreamName() {
      return this.logStreamName;
   }

   public String getFunctionName() {
      return this.functionName;
   }

   public String getFunctionVersion() {
      return this.functionVersion;
   }

   public String getInvokedFunctionArn() {
      return this.invokedFunctionArn;
   }

   public CognitoIdentity getIdentity() {
      return this.cognitoIdentity;
   }

   public ClientContext getClientContext() {
      return this.clientContext;
   }

   public int getRemainingTimeInMillis() {
      return lambdainternal.LambdaRuntime.getRemainingTime();
   }

   public LambdaLogger getLogger() {
      return this.logger;
   }
}
