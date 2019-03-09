package lambdainternal;

import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import lambdainternal.util.LambdaByteArrayOutputStream;
import lambdainternal.util.UnsafeUtil;

public final class HttpHandlerLoader {
   private HttpHandlerLoader() {
   }

   private static LambdaRequestHandler wrapHttpHandler(HandlerInfo handlerInfo) {
      try {
         return wrapHttpHandler(handlerInfo.clazz.getMethod(handlerInfo.methodName, InputStream.class, OutputStream.class));
      } catch (NoSuchMethodException var2) {
         return new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault("No method named " + handlerInfo.methodName + " with appropriate method signature found on class " + handlerInfo.clazz.getName()));
      }
   }

   private static LambdaRequestHandler wrapHttpHandler(final Method method) {
      return new LambdaRequestHandler() {
         public LambdaByteArrayOutputStream call(LambdaRuntime.InvokeRequest request) throws Error, Exception {
            if (request.sockfd < 0) {
               throw new UserFault("Socket cannot be negative -" + request.sockfd, (String)null, (String)null);
            } else {
               Socket socket = null;
               FileDescriptor fd = UnsafeUtil.toFd(request.sockfd);

               Object var4;
               try {
                  socket = UnsafeUtil.toSocket(fd);

                  try {
                     method.invoke((Object)null, socket.getInputStream(), socket.getOutputStream());
                  } catch (Throwable var8) {
                     throw UserFault.makeUserFault(UserFault.filterStackTrace(var8));
                  }

                  var4 = null;
               } finally {
                  if (socket != null) {
                     socket.close();
                  } else if (fd != null) {
                     UnsafeUtil.closeFd(fd);
                  }

               }

               return (LambdaByteArrayOutputStream)var4;
            }
         }
      };
   }

   public static LambdaRequestHandler loadHttpHandler(HandlerInfo handlerInfo) {
      return (LambdaRequestHandler)(handlerInfo.methodName == null ? new LambdaRequestHandler.UserFaultHandler(UserFault.makeUserFault("Invalid handler " + handlerInfo.clazz.getName() + ": class and method name should be separated by a double colon (::)")) : wrapHttpHandler(handlerInfo));
   }
}
