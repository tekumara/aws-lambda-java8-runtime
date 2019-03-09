package lambdainternal;

public class LambdaRuntime {
   public static final int MEMORY_LIMIT = Integer.parseInt(getEnv("AWS_LAMBDA_FUNCTION_MEMORY_SIZE"));
   public static final String LOG_GROUP_NAME = getEnv("AWS_LAMBDA_LOG_GROUP_NAME");
   public static final String LOG_STREAM_NAME = getEnv("AWS_LAMBDA_LOG_STREAM_NAME");
   public static final String FUNCTION_NAME = getEnv("AWS_LAMBDA_FUNCTION_NAME");
   public static final String FUNCTION_VERSION = getEnv("AWS_LAMBDA_FUNCTION_VERSION");
   public static volatile boolean needsDebugLogs = false;

   public LambdaRuntime() {
   }

   public static String getEnv(String envVariableName) {
      return System.getenv(envVariableName);
   }

   public static native void initRuntime();

   public static native void reportRunning(String var0);

   public static native void reportDone(String var0, byte[] var1, int var2, int var3);

   public static native void reportException(String var0);

   public static native void reportUserInitStart();

   public static native void reportUserInitEnd();

   public static native void reportUserInvokeStart();

   public static native void reportUserInvokeEnd();

   public static native void reportFault(String var0, String var1, String var2, String var3);

   public static native void setenv(String var0, String var1, int var2);

   public static native void writeSandboxLog(String var0);

   public static native LambdaRuntime.WaitForStartResult waitForStart();

   public static native LambdaRuntime.InvokeRequest waitForInvoke();

   public static native int getRemainingTime();

   public static native void sendContextLogs(byte[] var0, int var1);

   public static synchronized native void streamLogsToSlicer(byte[] var0, int var1, int var2);

   public static class WaitForStartResult {
      public final String invokeid;
      public final String handler;
      public final String mode;
      public final LambdaRuntime.AWSCredentials credentials;
      public final boolean suppressInit;

      public WaitForStartResult(String invokeid, String handler, String mode, String awskey, String awssecret, String awssession, boolean suppressInit) {
         this.invokeid = invokeid;
         this.handler = handler;
         this.mode = mode;
         this.credentials = new LambdaRuntime.AWSCredentials(awskey, awssecret, awssession);
         this.suppressInit = suppressInit;
      }
   }

   public static class InvokeRequest {
      public final int sockfd;
      public final String invokeid;
      public final String xAmznTraceId;
      public final LambdaRuntime.AWSCredentials credentials;
      public final String clientContext;
      public final String cognitoIdentityId;
      public final String cognitoPoolId;
      public final long eventBodyAddr;
      public final int eventBodyLen;
      public final boolean needsDebugLogs;
      public final String invokedFunctionArn;

      public InvokeRequest(int sockfd, String invokeid, String xAmznTraceId, String awskey, String awssecret, String awssession, String clientcontext, String cognitoidentityid, String cognitopoolid, long addr, int len, boolean needsDebugLogs, String invokedFunctionArn) {
         this.sockfd = sockfd;
         this.invokeid = invokeid;
         this.xAmznTraceId = xAmznTraceId;
         this.eventBodyAddr = addr;
         this.eventBodyLen = len;
         this.clientContext = clientcontext;
         this.cognitoIdentityId = cognitoidentityid;
         this.cognitoPoolId = cognitopoolid;
         this.credentials = new LambdaRuntime.AWSCredentials(awskey, awssecret, awssession);
         this.needsDebugLogs = needsDebugLogs;
         this.invokedFunctionArn = invokedFunctionArn;
      }
   }

   public static class AWSCredentials {
      public final String key;
      public final String secret;
      public final String session;

      public AWSCredentials(String key, String secret, String session) {
         this.key = key;
         this.secret = secret;
         this.session = session;
      }
   }
}
