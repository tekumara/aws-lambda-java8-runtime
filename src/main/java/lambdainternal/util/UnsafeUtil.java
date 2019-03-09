package lambdainternal.util;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketImpl;
import sun.misc.Unsafe;

public final class UnsafeUtil {
   public static final Unsafe TheUnsafe;

   private UnsafeUtil() {
   }

   public static RuntimeException throwException(Throwable t) {
      TheUnsafe.throwException(t);
      throw new Error("should never get here");
   }

   public static FileDescriptor toFd(int fd) throws RuntimeException {
      try {
         Class<FileDescriptor> clazz = FileDescriptor.class;
         Constructor<FileDescriptor> c = clazz.getDeclaredConstructor(Integer.TYPE);
         c.setAccessible(true);
         return (FileDescriptor)c.newInstance(new Integer(fd));
      } catch (Exception var3) {
         throw new RuntimeException(var3);
      }
   }

   public static void closeFd(FileDescriptor fd) throws IOException {
      (new FileOutputStream(fd)).close();
      if (fd.valid()) {
         System.err.println("File descriptor is still valid!");
      }

   }

   public static Socket toSocket(FileDescriptor fd) throws RuntimeException {
      try {
         Class<?> clazz = Class.forName("java.net.PlainSocketImpl");
         Constructor<?> c = clazz.getDeclaredConstructor(FileDescriptor.class);
         c.setAccessible(true);
         SocketImpl impl = (SocketImpl)c.newInstance(fd);
         clazz = Socket.class;
         c = clazz.getDeclaredConstructor(SocketImpl.class);
         c.setAccessible(true);
         Socket result = (Socket)c.newInstance(impl);
         Field created = clazz.getDeclaredField("created");
         created.setAccessible(true);
         created.setBoolean(result, true);
         Field connected = clazz.getDeclaredField("connected");
         connected.setAccessible(true);
         connected.setBoolean(result, true);
         Field bound = clazz.getDeclaredField("bound");
         bound.setAccessible(true);
         bound.setBoolean(result, true);
         return result;
      } catch (Exception var8) {
         throw new RuntimeException(var8);
      }
   }

   static {
      try {
         Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
         theUnsafe.setAccessible(true);
         TheUnsafe = (Unsafe)theUnsafe.get((Object)null);
      } catch (Exception var1) {
         throw new Error("failed to load Unsafe", var1);
      }
   }
}
