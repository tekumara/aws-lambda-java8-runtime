package lambdainternal;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Map;
import lambdainternal.api.LambdaContextLogger;
import lambdainternal.events.mixins.XRayStackTraceElementMixin;
import lambdainternal.serializerfactories.GsonFactory;
import lambdainternal.serializerfactories.JacksonFactory;
import lambdainternal.util.Functions;
import lambdainternal.util.LambdaByteArrayOutputStream;
import lambdainternal.util.LambdaOutputStream;
import lambdainternal.util.ReflectUtil;

public class AWSLambda {
   private static final Runnable doNothing = new Runnable() {
      public void run() {
      }
   };
   public static URLClassLoader customerClassLoader;

   public AWSLambda() {
   }

   private static final LambdaRequestHandler initErrorHandler(Throwable e, String className) {
      return new LambdaRequestHandler.UserFaultHandler(new UserFault("Error loading class " + className + (e.getMessage() == null ? "" : ": " + e.getMessage()), e.getClass().getName(), UserFault.trace(e), true));
   }

   private static final LambdaRequestHandler classNotFound(Throwable e, String className) {
      return new LambdaRequestHandler.UserFaultHandler(new UserFault("Class not found: " + className, e.getClass().getName(), UserFault.trace(e), false));
   }

   private static UserMethods findUserMethods(String handlerString, String mode, ClassLoader cl) {
      LambdaRuntime.reportUserInitStart();

      try {
         HandlerInfo handlerInfo;
         UserMethods var5;
         UserMethods var6;
         try {
            handlerInfo = HandlerInfo.fromString(handlerString, cl);
         } catch (HandlerInfo.InvalidHandlerException var15) {
            UserFault userFault = UserFault.makeUserFault("Invalid handler: `" + handlerString + "'");
            logInfo(String.format("Provided handler is invalid [invalidHandler] (Exception: %s )", Failure.getReportableExceptionClassName(var15)));
            var6 = new UserMethods(doNothing, new LambdaRequestHandler.UserFaultHandler(userFault));
            return var6;
         } catch (ClassNotFoundException var16) {
            logInfo(String.format("Unable to locate class [classNotFound] (Exception: %s )", Failure.getReportableExceptionClassName(var16)));
            var5 = new UserMethods(doNothing, classNotFound(var16, HandlerInfo.className(handlerString)));
            return var5;
         } catch (NoClassDefFoundError | ExceptionInInitializerError var17) {
            logInfo(String.format("Initialization error [initErrorHandler] (Exception: %s )", Failure.getReportableExceptionClassName(var17)));
            var5 = new UserMethods(doNothing, initErrorHandler(var17, HandlerInfo.className(handlerString)));
            return var5;
         } catch (Throwable var18) {
            logInfo(String.format("Unable to load customer class [throwable] (Exception: %s )", Failure.getReportableExceptionClassName(var18)));
            var5 = new UserMethods(doNothing, new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault(var18)));
            return var5;
         }

         byte var21 = -1;
         switch(mode.hashCode()) {
         case 3213448:
            if (mode.equals("http")) {
               var21 = 1;
            }
            break;
         case 96891546:
            if (mode.equals("event")) {
               var21 = 0;
            }
         }

         LambdaRequestHandler requestHandler;
         switch(var21) {
         case 0:
            requestHandler = EventHandlerLoader.loadEventHandler(handlerInfo);
            break;
         case 1:
            requestHandler = HttpHandlerLoader.loadHttpHandler(handlerInfo);
            break;
         default:
            throw new RuntimeException("invalid mode specified: " + mode);
         }

         Runnable initHandler = doNothing;

         try {
            initHandler = wrapInitCall(handlerInfo.clazz.getMethod("init"));
         } catch (NoClassDefFoundError | NoSuchMethodException var14) {
         }

         var6 = new UserMethods(initHandler, requestHandler);
         return var6;
      } finally {
         LambdaRuntime.reportUserInitEnd();
      }
   }

   private static void addIfNotNull(Map<String, String> envMap, String key, String value) {
      if (value != null && !value.isEmpty()) {
         envMap.put(key, value);
         LambdaRuntime.setenv(key, value, 1);
      }

   }

   private static void modifyEnv(Functions.V1<Map<String, String>> modifier) {
      try {
         Map<String, String> env = System.getenv();
         Field field = env.getClass().getDeclaredField("m");
         field.setAccessible(true);
         Object obj = field.get(env);
         Map<String, String> map = (Map)obj;
         modifier.call(map);
         field.setAccessible(false);
      } catch (Exception var5) {
         throw new RuntimeException(var5);
      }
   }

   private static void unsetLambdaInternalEnv() {
      modifyEnv((map) -> {
         String[] envList = new String[]{"_LAMBDA_SHARED_MEM_FD", "_LAMBDA_LOG_FD", "_LAMBDA_SB_ID", "_LAMBDA_CONSOLE_SOCKET", "_LAMBDA_CONTROL_SOCKET", "_LAMBDA_RUNTIME_LOAD_TIME"};
         String[] var2 = envList;
         int var3 = envList.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            String item = var2[var4];
            map.remove(item);
         }

      });
   }

   private static void setCredsEnv(LambdaRuntime.AWSCredentials creds, String region) {
      modifyEnv((map) -> {
         addIfNotNull(map, "AWS_ACCESS_KEY_ID", creds.key);
         addIfNotNull(map, "AWS_ACCESS_KEY", creds.key);
         addIfNotNull(map, "AWS_SECRET_ACCESS_KEY", creds.secret);
         addIfNotNull(map, "AWS_SECRET_KEY", creds.secret);
         addIfNotNull(map, "AWS_SESSION_TOKEN", creds.session);
      });
   }

   private static Runnable wrapInitCall(Method method) {
      return () -> {
         try {
            method.invoke((Object)null);
         } catch (Throwable var2) {
            throw UserFault.makeUserFault(var2);
         }
      };
   }

   public static void setupRuntimeLogger() throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
      ReflectUtil.setStaticField(Class.forName("com.amazonaws.services.lambda.runtime.LambdaRuntime"), "logger", true, new LambdaContextLogger());
   }

   public static void runInitHandler(Runnable initHandler, String invokeid) {
      try {
         initHandler.run();
      } catch (UserFault var3) {
         LambdaRuntime.reportFault(invokeid, var3.msg, var3.exception, var3.trace);
      }

   }

   public static void startRuntime(ClassLoader shared) throws Throwable {
      try {
         try {
            System.load("/var/runtime/lib/libawslambda.so");
         } catch (Throwable var21) {
            System.loadLibrary("awslambda");
         }

         LambdaRuntime.initRuntime();
      } catch (Throwable var22) {
         System.err.printf("Failed to load awslambda shared library: Library=%s Exception=%s message=%s%n", System.mapLibraryName("awslambda"), var22.getClass().getName(), var22.getMessage());
         var22.printStackTrace(System.err);
         System.exit(-1);
      }

      System.setOut(new PrintStream(new LambdaOutputStream(System.out), false, "UTF-8"));
      System.setErr(new PrintStream(new LambdaOutputStream(System.err), false, "UTF-8"));
      String awsRegion = LambdaRTEntry.getEnvOrExit("AWS_REGION");
      unsetLambdaInternalEnv();
      LambdaRuntime.WaitForStartResult startInfo = LambdaRuntime.waitForStart();
      setCredsEnv(startInfo.credentials, awsRegion);
      LambdaRuntime.reportRunning(startInfo.invokeid);
      logInfo(String.format("JVM runtime reported running (invokeId: %s)", startInfo.invokeid));
      String taskRoot = LambdaRTEntry.getEnvOrExit("LAMBDA_TASK_ROOT");
      String libRoot = "/opt/java";
      customerClassLoader = LambdaRTEntry.makeCustomerClassLoader(taskRoot, libRoot, shared);
      setupRuntimeLogger();
      Thread.currentThread().setContextClassLoader(customerClassLoader);
      UserMethods methods;
      if (startInfo.suppressInit) {
         methods = new UserMethods(doNothing, (LambdaRequestHandler)null);
      } else {
         methods = findUserMethods(startInfo.handler, startInfo.mode, customerClassLoader);
      }

      runInitHandler(methods.initHandler, startInfo.invokeid);
      LambdaRuntime.reportDone(startInfo.invokeid, (byte[])null, 0, 0);
      boolean shouldExit = false;

      while(true) {
         Throwable userException;
         UserFault userFault;
         LambdaRuntime.InvokeRequest request;
         LambdaByteArrayOutputStream message;
         int waitForExitFlag;
         while(true) {
            if (shouldExit) {
               return;
            }

            userException = null;
            userFault = null;
            request = LambdaRuntime.waitForInvoke();
            LambdaRuntime.needsDebugLogs = request.needsDebugLogs;
            if (request.xAmznTraceId != null) {
               modifyEnv((m) -> {
                  String var10000 = (String)m.put("_X_AMZN_TRACE_ID", request.xAmznTraceId);
               });
            } else {
               modifyEnv((m) -> {
                  String var10000 = (String)m.remove("_X_AMZN_TRACE_ID");
               });
            }

            if (methods.requestHandler == null) {
               methods = findUserMethods(startInfo.handler, startInfo.mode, customerClassLoader);
               runInitHandler(methods.initHandler, startInfo.invokeid);
            }

            setCredsEnv(request.credentials, (String)null);
            message = null;
            boolean var20 = false;

            label243: {
               try {
                  var20 = true;
                  LambdaRuntime.reportUserInvokeStart();
                  message = methods.requestHandler.call(request);
                  var20 = false;
                  break;
               } catch (UserFault var23) {
                  userFault = var23;
                  shouldExit = var23.fatal;
                  logInfo(String.format("Invoke failed with UserFault (Exception: %s, Fatal: %s )", Failure.getReportableExceptionClassName(var23), var23.fatal));
                  var20 = false;
               } catch (Throwable var24) {
                  userException = var24;
                  UserFault.filterStackTrace(var24);
                  userFault = UserFault.makeUserFault(var24);
                  message = new LambdaByteArrayOutputStream(1024);
                  Failure failure = new Failure(var24);
                  GsonFactory.getInstance().getSerializer(Failure.class).toJson(failure, message);
                  shouldExit = Failure.isInvokeFailureFatal(var24);
                  logInfo(String.format("Invoke failed with Throwable (Exception: %s, Fatal: %s )", Failure.getReportableExceptionClassName(var24), shouldExit));
                  var20 = false;
                  break label243;
               } finally {
                  if (var20) {
                     if (userFault != null) {
                        LambdaRuntime.reportFault(request.invokeid, userFault.msg, userFault.exception, userFault.trace);
                        LambdaRuntime.reportException(serializeAsXRayJson(userException));
                     }

                     LambdaRuntime.reportUserInvokeEnd();
                     int waitForExitFlag = shouldExit ? 1 : 0;
                     if (message == null) {
                        LambdaRuntime.reportDone(request.invokeid, (byte[])null, 0, waitForExitFlag);
                     } else {
                        LambdaRuntime.reportDone(request.invokeid, message.getRawBuf(), message.getValidByteCount(), waitForExitFlag);
                     }

                     LambdaRuntime.needsDebugLogs = false;
                  }
               }

               if (userFault != null) {
                  LambdaRuntime.reportFault(request.invokeid, userFault.msg, userFault.exception, userFault.trace);
                  LambdaRuntime.reportException(serializeAsXRayJson(userException));
               }

               LambdaRuntime.reportUserInvokeEnd();
               waitForExitFlag = shouldExit ? 1 : 0;
               if (message == null) {
                  LambdaRuntime.reportDone(request.invokeid, (byte[])null, 0, waitForExitFlag);
               } else {
                  LambdaRuntime.reportDone(request.invokeid, message.getRawBuf(), message.getValidByteCount(), waitForExitFlag);
               }

               LambdaRuntime.needsDebugLogs = false;
               continue;
            }

            if (userFault != null) {
               LambdaRuntime.reportFault(request.invokeid, userFault.msg, userFault.exception, userFault.trace);
               LambdaRuntime.reportException(serializeAsXRayJson(userException));
            }

            LambdaRuntime.reportUserInvokeEnd();
            waitForExitFlag = shouldExit ? 1 : 0;
            if (message == null) {
               LambdaRuntime.reportDone(request.invokeid, (byte[])null, 0, waitForExitFlag);
            } else {
               LambdaRuntime.reportDone(request.invokeid, message.getRawBuf(), message.getValidByteCount(), waitForExitFlag);
            }

            LambdaRuntime.needsDebugLogs = false;
         }

         if (userFault != null) {
            LambdaRuntime.reportFault(request.invokeid, userFault.msg, userFault.exception, userFault.trace);
            LambdaRuntime.reportException(serializeAsXRayJson(userException));
         }

         LambdaRuntime.reportUserInvokeEnd();
         waitForExitFlag = shouldExit ? 1 : 0;
         if (message == null) {
            LambdaRuntime.reportDone(request.invokeid, (byte[])null, 0, waitForExitFlag);
         } else {
            LambdaRuntime.reportDone(request.invokeid, message.getRawBuf(), message.getValidByteCount(), waitForExitFlag);
         }

         LambdaRuntime.needsDebugLogs = false;
      }
   }

   private static String serializeAsXRayJson(Throwable throwable) {
      try {
         OutputStream outputStream = new ByteArrayOutputStream();
         XRayErrorCause cause = new XRayErrorCause(throwable);
         JacksonFactory.getInstance().withNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES).withMixin(StackTraceElement.class, XRayStackTraceElementMixin.class).getSerializer(XRayErrorCause.class).toJson(cause, outputStream);
         return outputStream.toString();
      } catch (Exception var3) {
         return null;
      }
   }

   private static final void logInfo(String msg) {
      try {
         LambdaRuntime.writeSandboxLog(String.format("[INFO] (%s) %s", Thread.currentThread().getStackTrace()[2], msg));
      } catch (Throwable var2) {
      }

   }

   static {
      try {
         startRuntime(ClassLoader.getSystemClassLoader());
      } catch (Throwable var1) {
         throw new Error(var1);
      }
   }
}
