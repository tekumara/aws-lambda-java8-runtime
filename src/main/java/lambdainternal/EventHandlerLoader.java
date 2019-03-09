package lambdainternal;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaRuntimeInternal;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import lambdainternal.api.LambdaClientContext;
import lambdainternal.api.LambdaCognitoIdentity;
import lambdainternal.api.LambdaContext;
import lambdainternal.events.EventConfiguration;
import lambdainternal.serializerfactories.GsonFactory;
import lambdainternal.serializerfactories.JacksonFactory;
import lambdainternal.serializerfactories.PojoSerializerFactory;
import lambdainternal.util.Functions;
import lambdainternal.util.LambdaByteArrayOutputStream;
import lambdainternal.util.NativeMemoryAsInputStream;
import lambdainternal.util.ReflectUtil;
import lambdainternal.util.UnsafeUtil;

public final class EventHandlerLoader {
   private static final byte[] _JsonNull = new byte[]{110, 117, 108, 108};
   private static final EnumMap<EventHandlerLoader.Platform, Map<Type, PojoSerializerFactory.PojoSerializer<Object>>> typeCache = new EnumMap(EventHandlerLoader.Platform.class);
   private static volatile PojoSerializerFactory.PojoSerializer<LambdaClientContext> contextSerializer;
   private static final Comparator<Method> methodPriority = new Comparator<Method>() {
      public int compare(Method lhs, Method rhs) {
         if (!lhs.isBridge() && rhs.isBridge()) {
            return -1;
         } else if (!rhs.isBridge() && lhs.isBridge()) {
            return 1;
         } else {
            Class<?>[] lParams = lhs.getParameterTypes();
            Class<?>[] rParams = rhs.getParameterTypes();
            int lParamCompareLength = lParams.length;
            int rParamCompareLength = rParams.length;
            if (EventHandlerLoader.lastParameterIsContext(lParams)) {
               ++lParamCompareLength;
            }

            if (EventHandlerLoader.lastParameterIsContext(rParams)) {
               ++rParamCompareLength;
            }

            return -Integer.compare(lParamCompareLength, rParamCompareLength);
         }
      }
   };

   private EventHandlerLoader() {
   }

   private static PojoSerializerFactory.PojoSerializer<Object> getSerializer(EventHandlerLoader.Platform platform, Type type) {
      if (type instanceof Class) {
         Class<Object> clazz = (Class)type;
         if (EventConfiguration.isLambdaSupportedEvent(clazz.getName())) {
            return EventConfiguration.getEventSerializerFor(clazz);
         }
      }

      switch(platform) {
      case ANDROID:
         return GsonFactory.getInstance().getSerializer(type);
      default:
         return JacksonFactory.getInstance().getSerializer(type);
      }
   }

   private static PojoSerializerFactory.PojoSerializer<Object> getSerializerCached(EventHandlerLoader.Platform platform, Type type) {
      Map<Type, PojoSerializerFactory.PojoSerializer<Object>> cache = (Map)typeCache.get(platform);
      if (cache == null) {
         cache = new HashMap();
         typeCache.put(platform, cache);
      }

      PojoSerializerFactory.PojoSerializer<Object> serializer = (PojoSerializerFactory.PojoSerializer)((Map)cache).get(type);
      if (serializer == null) {
         serializer = getSerializer(platform, type);
         ((Map)cache).put(type, serializer);
      }

      return serializer;
   }

   private static PojoSerializerFactory.PojoSerializer<LambdaClientContext> getContextSerializer() {
      if (contextSerializer == null) {
         contextSerializer = GsonFactory.getInstance().getSerializer(LambdaClientContext.class);
      }

      return contextSerializer;
   }

   private static EventHandlerLoader.Platform getPlatform(Context context) {
      ClientContext cc = context.getClientContext();
      if (cc == null) {
         return EventHandlerLoader.Platform.UNKNOWN;
      } else {
         Map<String, String> env = cc.getEnvironment();
         if (env == null) {
            return EventHandlerLoader.Platform.UNKNOWN;
         } else {
            String platform = (String)env.get("platform");
            if (platform == null) {
               return EventHandlerLoader.Platform.UNKNOWN;
            } else if ("Android".equalsIgnoreCase(platform)) {
               return EventHandlerLoader.Platform.ANDROID;
            } else {
               return "iPhoneOS".equalsIgnoreCase(platform) ? EventHandlerLoader.Platform.IOS : EventHandlerLoader.Platform.UNKNOWN;
            }
         }
      }
   }

   private static boolean isVoid(Type type) {
      return Void.TYPE.equals(type) || type instanceof Class && Void.class.isAssignableFrom((Class)type);
   }

   public static <T> Constructor<T> getConstructor(Class<T> clazz) throws Exception {
      try {
         Constructor<T> constructor = clazz.getConstructor();
         return constructor;
      } catch (NoSuchMethodException var3) {
         if (clazz.getEnclosingClass() != null && !Modifier.isStatic(clazz.getModifiers())) {
            throw new Exception("Class " + clazz.getName() + " cannot be instantiated because it is a non-static inner class");
         } else {
            throw new Exception("Class " + clazz.getName() + " has no public zero-argument constructor", var3);
         }
      }
   }

   public static <T> T newInstance(Constructor<? extends T> constructor) {
      try {
         return constructor.newInstance();
      } catch (UserFault var2) {
         throw var2;
      } catch (InstantiationException | InvocationTargetException var3) {
         throw UnsafeUtil.throwException((Throwable)(var3.getCause() == null ? var3 : var3.getCause()));
      } catch (IllegalAccessException var4) {
         throw UnsafeUtil.throwException(var4);
      }
   }

   public static Type[] findInterfaceParameters(Class<?> clazz, Class<?> iface) {
      LinkedList<EventHandlerLoader.ClassContext> clazzes = new LinkedList();
      clazzes.addFirst(new EventHandlerLoader.ClassContext(clazz, (Type[])null));

      while(!clazzes.isEmpty()) {
         EventHandlerLoader.ClassContext curContext = (EventHandlerLoader.ClassContext)clazzes.removeLast();
         Type[] interfaces = curContext.clazz.getGenericInterfaces();
         Type[] var5 = interfaces;
         int var6 = interfaces.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            Type type = var5[var7];
            if (type instanceof ParameterizedType) {
               ParameterizedType candidate = (ParameterizedType)type;
               Type rawType = candidate.getRawType();
               if (!(rawType instanceof Class)) {
                  System.err.println("raw type is not a class: " + rawType);
               } else {
                  Class<?> rawClass = (Class)rawType;
                  if (iface.isAssignableFrom(rawClass)) {
                     return (new EventHandlerLoader.ClassContext(candidate, curContext)).actualTypeArguments;
                  }

                  clazzes.addFirst(new EventHandlerLoader.ClassContext(candidate, curContext));
               }
            } else if (type instanceof Class) {
               clazzes.addFirst(new EventHandlerLoader.ClassContext((Class)type, curContext));
            } else {
               System.err.println("Unexpected type class " + type.getClass().getName());
            }
         }

         Type superClass = curContext.clazz.getGenericSuperclass();
         if (superClass instanceof ParameterizedType) {
            clazzes.addFirst(new EventHandlerLoader.ClassContext((ParameterizedType)superClass, curContext));
         } else if (superClass != null) {
            clazzes.addFirst(new EventHandlerLoader.ClassContext((Class)superClass, curContext));
         }
      }

      return null;
   }

   public static LambdaRequestHandler wrapRequestHandlerClass(Class<? extends RequestHandler> clazz) {
      Type[] ptypes = findInterfaceParameters(clazz, RequestHandler.class);
      if (ptypes == null) {
         return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault("Class " + clazz.getName() + " does not implement RequestHandler with concrete type parameters"));
      } else if (ptypes.length != 2) {
         return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault("Invalid class signature for RequestHandler. Expected two generic types, got " + ptypes.length));
      } else {
         Type[] var2 = ptypes;
         int var3 = ptypes.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Type t = var2[var4];
            if (t instanceof TypeVariable) {
               Type[] bounds = ((TypeVariable)t).getBounds();
               boolean foundBound = false;
               if (bounds != null) {
                  Type[] var8 = bounds;
                  int var9 = bounds.length;

                  for(int var10 = 0; var10 < var9; ++var10) {
                     Type bound = var8[var10];
                     if (!Object.class.equals(bound)) {
                        foundBound = true;
                        break;
                     }
                  }
               }

               if (!foundBound) {
                  return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault("Class " + clazz.getName() + " does not implement RequestHandler with concrete type parameters: parameter " + t + " has no upper bound."));
               }
            }
         }

         Type pType = ptypes[0];
         Type rType = ptypes[1];

         try {
            Constructor<? extends RequestHandler> constructor = getConstructor(clazz);
            return wrapPojoHandler((RequestHandler)newInstance(constructor), pType, rType);
         } catch (Throwable var12) {
            return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault(var12));
         }
      }
   }

   public static LambdaRequestHandler wrapRequestStreamHandlerClass(Class<? extends RequestStreamHandler> clazz) {
      try {
         Constructor<? extends RequestStreamHandler> constructor = getConstructor(clazz);
         return wrapRequestStreamHandler((RequestStreamHandler)newInstance(constructor));
      } catch (Throwable var3) {
         return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault(var3));
      }
   }

   public static LambdaRequestHandler loadStreamingRequestHandler(Class<?> clazz) {
      if (RequestStreamHandler.class.isAssignableFrom(clazz)) {
         return wrapRequestStreamHandlerClass(clazz.asSubclass(RequestStreamHandler.class));
      } else {
         return (LambdaRequestHandler)(RequestHandler.class.isAssignableFrom(clazz) ? wrapRequestHandlerClass(clazz.asSubclass(RequestHandler.class)) : new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault("Class does not implement an appropriate handler interface: " + clazz.getName())));
      }
   }

   public static LambdaRequestHandler loadEventHandler(HandlerInfo handlerInfo) {
      return handlerInfo.methodName == null ? loadStreamingRequestHandler(handlerInfo.clazz) : loadEventPojoHandler(handlerInfo);
   }

   private static Optional<LambdaRequestHandler> getOneLengthHandler(Class<?> clazz, Method m, Type pType, Type rType) {
      if (InputStream.class.equals(pType)) {
         return Optional.of(EventHandlerLoader.StreamMethodRequestHandler.makeRequestHandler(clazz, m, true, false, false));
      } else if (OutputStream.class.equals(pType)) {
         return Optional.of(EventHandlerLoader.StreamMethodRequestHandler.makeRequestHandler(clazz, m, false, true, false));
      } else {
         return isContext(pType) ? Optional.of(EventHandlerLoader.PojoMethodRequestHandler.makeRequestHandler(clazz, m, (Type)null, rType, true)) : Optional.of(EventHandlerLoader.PojoMethodRequestHandler.makeRequestHandler(clazz, m, pType, rType, false));
      }
   }

   private static Optional<LambdaRequestHandler> getTwoLengthHandler(Class<?> clazz, Method m, Type pType1, Type pType2, Type rType) {
      if (OutputStream.class.equals(pType1)) {
         if (isContext(pType2)) {
            return Optional.of(EventHandlerLoader.StreamMethodRequestHandler.makeRequestHandler(clazz, m, false, true, true));
         } else {
            System.err.println("Ignoring two-argument overload because first argument type is OutputStream and second argument type is not Context");
            return Optional.empty();
         }
      } else if (isContext(pType1)) {
         System.err.println("Ignoring two-argument overload because first argument type is Context");
         return Optional.empty();
      } else if (InputStream.class.equals(pType1)) {
         if (OutputStream.class.equals(pType2)) {
            return Optional.of(EventHandlerLoader.StreamMethodRequestHandler.makeRequestHandler(clazz, m, true, true, false));
         } else if (isContext(pType2)) {
            return Optional.of(EventHandlerLoader.StreamMethodRequestHandler.makeRequestHandler(clazz, m, true, false, true));
         } else {
            System.err.println("Ignoring two-argument overload because second parameter type, " + ReflectUtil.getRawClass(pType2).getName() + ", is not OutputStream.");
            return Optional.empty();
         }
      } else if (isContext(pType2)) {
         return Optional.of(EventHandlerLoader.PojoMethodRequestHandler.makeRequestHandler(clazz, m, pType1, rType, true));
      } else {
         System.err.println("Ignoring two-argument overload because second parameter type is not Context");
         return Optional.empty();
      }
   }

   private static Optional<LambdaRequestHandler> getThreeLengthHandler(Class<?> clazz, Method m, Type pType1, Type pType2, Type pType3, Type rType) {
      if (InputStream.class.equals(pType1) && OutputStream.class.equals(pType2) && isContext(pType3)) {
         return Optional.of(EventHandlerLoader.StreamMethodRequestHandler.makeRequestHandler(clazz, m, true, true, true));
      } else {
         System.err.println("Ignoring three-argument overload because argument signature is not (InputStream, OutputStream, Context");
         return Optional.empty();
      }
   }

   private static Optional<LambdaRequestHandler> getHandlerFromOverload(Class<?> clazz, Method m) {
      Type rType = m.getGenericReturnType();
      Type[] pTypes = m.getGenericParameterTypes();
      if (pTypes.length == 0) {
         return Optional.of(EventHandlerLoader.PojoMethodRequestHandler.makeRequestHandler(clazz, m, (Type)null, rType, false));
      } else if (pTypes.length == 1) {
         return getOneLengthHandler(clazz, m, pTypes[0], rType);
      } else if (pTypes.length == 2) {
         return getTwoLengthHandler(clazz, m, pTypes[0], pTypes[1], rType);
      } else if (pTypes.length == 3) {
         return getThreeLengthHandler(clazz, m, pTypes[0], pTypes[1], pTypes[2], rType);
      } else {
         System.err.println("Ignoring an overload of method " + m.getName() + " because it has too many parameters: Expected at most 3, got " + pTypes.length);
         return Optional.empty();
      }
   }

   private static final boolean isContext(Type t) {
      return Context.class.equals(t);
   }

   private static final boolean lastParameterIsContext(Class<?>[] params) {
      return params.length != 0 && isContext(params[params.length - 1]);
   }

   private static LambdaRequestHandler loadEventPojoHandler(HandlerInfo handlerInfo) {
      Method[] methods;
      try {
         methods = handlerInfo.clazz.getMethods();
      } catch (NoClassDefFoundError var7) {
         return new LambdaRequestHandler.UserFaultHandler(new UserFault("Error loading method " + handlerInfo.methodName + " on class " + handlerInfo.clazz.getName(), var7.getClass().getName(), UserFault.trace(var7)));
      }

      if (methods.length == 0) {
         String msg = "Class " + handlerInfo.getClass().getName() + " has no public method named " + handlerInfo.methodName;
         return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault(msg));
      } else {
         int slide = 0;

         int end;
         for(end = 0; end < methods.length; ++end) {
            Method m = methods[end];
            methods[end - slide] = m;
            if (!m.getName().equals(handlerInfo.methodName)) {
               ++slide;
            }
         }

         end = methods.length - slide;
         Arrays.sort(methods, 0, end, methodPriority);

         for(int i = 0; i < end; ++i) {
            Method m = methods[i];
            Optional<LambdaRequestHandler> result = getHandlerFromOverload(handlerInfo.clazz, m);
            if (result.isPresent()) {
               return (LambdaRequestHandler)result.get();
            }
         }

         return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault("No public method named " + handlerInfo.methodName + " with appropriate method signature found on class " + handlerInfo.clazz));
      }
   }

   public static LambdaRequestHandler wrapPojoHandler(RequestHandler instance, Type pType, Type rType) {
      return wrapRequestStreamHandler((RequestStreamHandler)(new EventHandlerLoader.PojoHandlerAsStreamHandler(instance, Optional.ofNullable(pType), isVoid(rType) ? Optional.empty() : Optional.of(rType))));
   }

   public static String exceptionToString(Throwable t) {
      StringWriter writer = new StringWriter(65536);
      PrintWriter wrapped = new PrintWriter(writer);
      Throwable var3 = null;

      try {
         t.printStackTrace(wrapped);
      } catch (Throwable var12) {
         var3 = var12;
         throw var12;
      } finally {
         if (wrapped != null) {
            if (var3 != null) {
               try {
                  wrapped.close();
               } catch (Throwable var11) {
                  var3.addSuppressed(var11);
               }
            } else {
               wrapped.close();
            }
         }

      }

      StringBuffer buffer = writer.getBuffer();
      if (buffer.length() > 262144) {
         String extra = " Truncated by Lambda";
         buffer.delete(262144, buffer.length());
         buffer.append(" Truncated by Lambda");
      }

      return buffer.toString();
   }

   public static LambdaRequestHandler wrapRequestStreamHandler(Constructor<? extends RequestStreamHandler> constructor) {
      return wrapRequestStreamHandler((RequestStreamHandler)newInstance(constructor));
   }

   public static LambdaRequestHandler wrapRequestStreamHandler(final RequestStreamHandler handler) {
      return new LambdaRequestHandler() {
         private final LambdaByteArrayOutputStream output = new LambdaByteArrayOutputStream(1024);
         private Functions.V2<String, String> log4jContextPutMethod = null;

         private void safeAddRequestIdToLog4j(String log4jContextClassName, LambdaRuntime.InvokeRequest request, Class contextMapValueClass) {
            try {
               Class<?> log4jContextClass = ReflectUtil.loadClass(AWSLambda.customerClassLoader, log4jContextClassName);
               this.log4jContextPutMethod = ReflectUtil.loadStaticV2(log4jContextClass, "put", false, String.class, contextMapValueClass);
               this.log4jContextPutMethod.call("AWSRequestId", request.invokeid);
            } catch (Exception var5) {
            }

         }

         public LambdaByteArrayOutputStream call(LambdaRuntime.InvokeRequest request) throws Error, Exception {
            if (request.sockfd >= 0) {
               throw new UserFault("Invalid args - eventbody = " + request.eventBodyAddr + " socket =" + request.sockfd, (String)null, (String)null);
            } else {
               this.output.reset();
               LambdaCognitoIdentity cognitoIdentity = new LambdaCognitoIdentity(request.cognitoIdentityId, request.cognitoPoolId);
               LambdaClientContext clientContext = null;
               if (request.clientContext != null && request.clientContext.length() > 0) {
                  try {
                     clientContext = (LambdaClientContext)EventHandlerLoader.getContextSerializer().fromJson(request.clientContext);
                  } catch (Throwable var7) {
                     UserFault.filterStackTrace(var7);
                     UserFault f = UserFault.makeUserFault("Error parsing Client Context as JSON");
                     LambdaRuntime.reportFault(request.invokeid, f.msg, f.exception, f.trace);
                     Failure failure = new Failure(var7);
                     GsonFactory.getInstance().getSerializer(Failure.class).toJson(failure, this.output);
                     return this.output;
                  }
               }

               LambdaContext context = new LambdaContext(LambdaRuntime.MEMORY_LIMIT, request.invokeid, LambdaRuntime.LOG_GROUP_NAME, LambdaRuntime.LOG_STREAM_NAME, LambdaRuntime.FUNCTION_NAME, cognitoIdentity, LambdaRuntime.FUNCTION_VERSION, request.invokedFunctionArn, clientContext);
               if (LambdaRuntimeInternal.getUseLog4jAppender()) {
                  this.safeAddRequestIdToLog4j("org.apache.log4j.MDC", request, Object.class);
                  this.safeAddRequestIdToLog4j("org.apache.logging.log4j.ThreadContext", request, String.class);
                  if (this.log4jContextPutMethod == null) {
                     System.err.println("Customer using log4j appender but unable to load either org.apache.log4j.MDC or org.apache.logging.log4j.ThreadContext. Customer cannot see RequestId in log4j log lines.");
                  }
               }

               NativeMemoryAsInputStream stream = new NativeMemoryAsInputStream(request.eventBodyAddr, request.eventBodyAddr + (long)request.eventBodyLen);
               handler.handleRequest(stream, this.output, context);
               return this.output;
            }
         }
      };
   }

   private static final class ClassContext {
      public final Class<?> clazz;
      public final Type[] actualTypeArguments;
      private TypeVariable[] typeParameters;

      public ClassContext(Class<?> clazz, Type[] actualTypeArguments) {
         this.clazz = clazz;
         this.actualTypeArguments = actualTypeArguments;
      }

      public ClassContext(Class<?> clazz, EventHandlerLoader.ClassContext curContext) {
         this.typeParameters = clazz.getTypeParameters();
         if (this.typeParameters.length != 0 && curContext.actualTypeArguments != null) {
            Type[] types = new Type[this.typeParameters.length];

            for(int i = 0; i < types.length; ++i) {
               types[i] = curContext.resolveTypeVariable(this.typeParameters[i]);
            }

            this.clazz = clazz;
            this.actualTypeArguments = types;
         } else {
            this.clazz = clazz;
            this.actualTypeArguments = null;
         }

      }

      public ClassContext(ParameterizedType type, EventHandlerLoader.ClassContext curContext) {
         Type[] types = type.getActualTypeArguments();

         for(int i = 0; i < types.length; ++i) {
            Type t = types[i];
            if (t instanceof TypeVariable) {
               types[i] = curContext.resolveTypeVariable((TypeVariable)t);
            }
         }

         Type t = type.getRawType();
         if (t instanceof Class) {
            this.clazz = (Class)t;
         } else {
            if (!(t instanceof TypeVariable)) {
               throw new RuntimeException("Type " + t + " is of unexpected type " + t.getClass());
            }

            this.clazz = (Class)((TypeVariable)t).getGenericDeclaration();
         }

         this.actualTypeArguments = types;
      }

      public Type resolveTypeVariable(TypeVariable t) {
         TypeVariable[] variables = this.getTypeParameters();

         for(int i = 0; i < variables.length; ++i) {
            if (t.getName().equals(variables[i].getName())) {
               return (Type)(this.actualTypeArguments == null ? variables[i] : this.actualTypeArguments[i]);
            }
         }

         return t;
      }

      private TypeVariable[] getTypeParameters() {
         if (this.typeParameters == null) {
            this.typeParameters = this.clazz.getTypeParameters();
         }

         return this.typeParameters;
      }
   }

   private static final class StreamMethodRequestHandler implements RequestStreamHandler {
      public final Method m;
      public final Object instance;
      public final boolean needsInput;
      public final boolean needsOutput;
      public final boolean needsContext;
      public final int argSize;

      public StreamMethodRequestHandler(Method m, Object instance, boolean needsInput, boolean needsOutput, boolean needsContext) {
         this.m = m;
         this.instance = instance;
         this.needsInput = needsInput;
         this.needsOutput = needsOutput;
         this.needsContext = needsContext;
         this.argSize = (needsInput ? 1 : 0) + (needsOutput ? 1 : 0) + (needsContext ? 1 : 0);
      }

      public static EventHandlerLoader.StreamMethodRequestHandler fromMethod(Class<?> clazz, Method m, boolean needsInput, boolean needsOutput, boolean needsContext) throws Exception {
         if (!EventHandlerLoader.isVoid(m.getReturnType())) {
            System.err.println("Will ignore return type " + m.getReturnType() + " on byte stream handler");
         }

         Object instance = Modifier.isStatic(m.getModifiers()) ? null : EventHandlerLoader.newInstance(EventHandlerLoader.getConstructor(clazz));
         return new EventHandlerLoader.StreamMethodRequestHandler(m, instance, needsInput, needsOutput, needsContext);
      }

      public static LambdaRequestHandler makeRequestHandler(Class<?> clazz, Method m, boolean needsInput, boolean needsOutput, boolean needsContext) {
         try {
            return EventHandlerLoader.wrapRequestStreamHandler((RequestStreamHandler)fromMethod(clazz, m, needsInput, needsOutput, needsContext));
         } catch (Throwable var6) {
            return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault(var6));
         }
      }

      public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
         Object[] args = new Object[this.argSize];
         int idx = 0;
         if (this.needsInput) {
            args[idx++] = inputStream;
         } else {
            inputStream.close();
         }

         if (this.needsOutput) {
            args[idx++] = outputStream;
         }

         if (this.needsContext) {
            args[idx++] = context;
         }

         try {
            this.m.invoke(this.instance, args);
            if (!this.needsOutput) {
               outputStream.write(EventHandlerLoader._JsonNull);
            }

         } catch (InvocationTargetException var7) {
            if (var7.getCause() != null) {
               throw UnsafeUtil.throwException(UserFault.filterStackTrace(var7.getCause()));
            } else {
               throw UnsafeUtil.throwException(UserFault.filterStackTrace(var7));
            }
         } catch (Throwable var8) {
            throw UnsafeUtil.throwException(UserFault.filterStackTrace(var8));
         }
      }
   }

   private static final class PojoMethodRequestHandler implements RequestHandler<Object, Object> {
      public final Method m;
      public final Type pType;
      public final Object instance;
      public final boolean needsContext;
      public final int argSize;

      public PojoMethodRequestHandler(Method m, Type pType, Type rType, Object instance, boolean needsContext) {
         this.m = m;
         this.pType = pType;
         this.instance = instance;
         this.needsContext = needsContext;
         this.argSize = (needsContext ? 1 : 0) + (pType != null ? 1 : 0);
      }

      public static EventHandlerLoader.PojoMethodRequestHandler fromMethod(Class<?> clazz, Method m, Type pType, Type rType, boolean needsContext) throws Exception {
         Object instance;
         if (Modifier.isStatic(m.getModifiers())) {
            instance = null;
         } else {
            instance = EventHandlerLoader.newInstance(EventHandlerLoader.getConstructor(clazz));
         }

         return new EventHandlerLoader.PojoMethodRequestHandler(m, pType, rType, instance, needsContext);
      }

      public static LambdaRequestHandler makeRequestHandler(Class<?> clazz, Method m, Type pType, Type rType, boolean needsContext) {
         try {
            return EventHandlerLoader.wrapPojoHandler(fromMethod(clazz, m, pType, rType, needsContext), pType, rType);
         } catch (Throwable var6) {
            return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault(var6));
         }
      }

      public Object handleRequest(Object input, Context context) {
         Object[] args = new Object[this.argSize];
         int idx = 0;
         if (this.pType != null) {
            args[idx++] = input;
         }

         if (this.needsContext) {
            args[idx++] = context;
         }

         try {
            return this.m.invoke(this.instance, args);
         } catch (InvocationTargetException var6) {
            if (var6.getCause() != null) {
               throw UnsafeUtil.throwException(UserFault.filterStackTrace(var6.getCause()));
            } else {
               throw UnsafeUtil.throwException(UserFault.filterStackTrace(var6));
            }
         } catch (Throwable var7) {
            throw UnsafeUtil.throwException(UserFault.filterStackTrace(var7));
         }
      }
   }

   private static final class PojoHandlerAsStreamHandler implements RequestStreamHandler {
      public RequestHandler innerHandler;
      public final Optional<Type> inputType;
      public final Optional<Type> outputType;

      public PojoHandlerAsStreamHandler(RequestHandler innerHandler, Optional<Type> inputType, Optional<Type> outputType) {
         this.innerHandler = innerHandler;
         this.inputType = inputType;
         this.outputType = outputType;
         if (inputType.isPresent()) {
            EventHandlerLoader.getSerializerCached(EventHandlerLoader.Platform.UNKNOWN, (Type)inputType.get());
         }

         if (outputType.isPresent()) {
            EventHandlerLoader.getSerializerCached(EventHandlerLoader.Platform.UNKNOWN, (Type)outputType.get());
         }

      }

      public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
         EventHandlerLoader.Platform platform = EventHandlerLoader.getPlatform(context);

         Object input;
         try {
            if (this.inputType.isPresent()) {
               input = EventHandlerLoader.getSerializerCached(platform, (Type)this.inputType.get()).fromJson(inputStream);
            } else {
               input = null;
            }
         } catch (Throwable var10) {
            throw new RuntimeException("An error occurred during JSON parsing", UserFault.filterStackTrace(var10));
         }

         Object output;
         try {
            output = this.innerHandler.handleRequest(input, context);
         } catch (Throwable var9) {
            throw UnsafeUtil.throwException(UserFault.filterStackTrace(var9));
         }

         try {
            if (this.outputType.isPresent()) {
               PojoSerializerFactory.PojoSerializer<Object> serializer = EventHandlerLoader.getSerializerCached(platform, (Type)this.outputType.get());
               serializer.toJson(output, outputStream);
            } else {
               outputStream.write(EventHandlerLoader._JsonNull);
            }

         } catch (Throwable var8) {
            throw new RuntimeException("An error occurred during JSON serialization of response", var8);
         }
      }
   }

   private static enum Platform {
      ANDROID,
      IOS,
      UNKNOWN;

      private Platform() {
      }
   }
}
