package lambdainternal.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public final class ReflectUtil {
   private ReflectUtil() {
   }

   public static Class<?> copyClass(Class<?> clazz, ClassLoader cl) {
      try {
         return cl.loadClass(clazz.getName());
      } catch (ClassNotFoundException var22) {
         int chunkSize = true;
         String resourceName = clazz.getName().replace('.', '/') + ".class";

         LambdaByteArrayOutputStream stream;
         try {
            InputStream input = clazz.getClassLoader().getResourceAsStream(resourceName);
            Throwable var6 = null;

            try {
               int initial = Math.max(1024, input.available());
               stream = new LambdaByteArrayOutputStream(initial);
               stream.readAll(input);
            } catch (Throwable var19) {
               var6 = var19;
               throw var19;
            } finally {
               if (input != null) {
                  if (var6 != null) {
                     try {
                        input.close();
                     } catch (Throwable var18) {
                        var6.addSuppressed(var18);
                     }
                  } else {
                     input.close();
                  }
               }

            }
         } catch (IOException var21) {
            throw new UncheckedIOException(var21);
         }

         try {
            Functions.R5<Class<?>, ClassLoader, String, byte[], Integer, Integer> defineClassMethod = loadInstanceR4(ClassLoader.class, "defineClass", true, Class.class, String.class, byte[].class, Integer.TYPE, Integer.TYPE);
            Class<?> result = (Class)defineClassMethod.call(cl, clazz.getName(), stream.getRawBuf(), 0, stream.getValidByteCount());
            Functions.V2<ClassLoader, Class<?>> resolveClass = loadInstanceV1(ClassLoader.class, "resolveClass", true, Class.class);
            resolveClass.call(cl, result);
            return result;
         } catch (SecurityException | ClassFormatError var17) {
            throw new ReflectUtil.ReflectException(var17);
         }
      }
   }

   public static Class<?> loadClass(ClassLoader cl, String name) {
      try {
         return Class.forName(name, true, cl);
      } catch (LinkageError | ClassNotFoundException var3) {
         throw new ReflectUtil.ReflectException(var3);
      }
   }

   private static <T> T newInstance(Constructor<? extends T> constructor, Object... params) {
      try {
         return constructor.newInstance(params);
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException var3) {
         throw new ReflectUtil.ReflectException(var3);
      }
   }

   public static Class<?> getRawClass(Type type) {
      if (type instanceof Class) {
         return (Class)type;
      } else if (type instanceof ParameterizedType) {
         return getRawClass(((ParameterizedType)type).getRawType());
      } else if (type instanceof GenericArrayType) {
         Class<?> componentRaw = getRawClass(((GenericArrayType)type).getGenericComponentType());
         return Array.newInstance(componentRaw, 0).getClass();
      } else if (type instanceof TypeVariable) {
         throw new ReflectUtil.ReflectException("type variables not supported");
      } else {
         throw new ReflectUtil.ReflectException("unsupport type: " + type.getClass().getName());
      }
   }

   public static Functions.R1<Object, Object> makeCaster(Type type) {
      return makeCaster(getRawClass(type));
   }

   private static <T> Functions.R1<T, Object> boxCaster(final Class<? extends T> clazz) {
      return new Functions.R1<T, Object>() {
         public T call(Object o) {
            return clazz.cast(o);
         }
      };
   }

   public static <T> Functions.R1<T, Object> makeCaster(Class<? extends T> clazz) {
      if (Long.TYPE.equals(clazz)) {
         return boxCaster(Long.class);
      } else if (Double.TYPE.equals(clazz)) {
         return boxCaster(Double.class);
      } else if (Float.TYPE.equals(clazz)) {
         return boxCaster(Float.class);
      } else if (Integer.TYPE.equals(clazz)) {
         return boxCaster(Integer.class);
      } else if (Short.TYPE.equals(clazz)) {
         return boxCaster(Short.class);
      } else if (Character.TYPE.equals(clazz)) {
         return boxCaster(Character.class);
      } else if (Byte.TYPE.equals(clazz)) {
         return boxCaster(Byte.class);
      } else {
         return Boolean.TYPE.equals(clazz) ? boxCaster(Boolean.class) : boxCaster(clazz);
      }
   }

   private static <T> T invoke(Method method, Object instance, Class<? extends T> rType, Object... params) {
      Functions.R1 caster = makeCaster(rType);

      try {
         Object result = method.invoke(instance, params);
         return rType.equals(Void.TYPE) ? null : caster.call(result);
      } catch (ExceptionInInitializerError | IllegalAccessException | InvocationTargetException var6) {
         throw new ReflectUtil.ReflectException(var6);
      }
   }

   private static Method lookupMethod(Class<?> clazz, String name, Class... pTypes) {
      try {
         try {
            return clazz.getDeclaredMethod(name, pTypes);
         } catch (NoSuchMethodException var4) {
            return clazz.getMethod(name, pTypes);
         }
      } catch (SecurityException | NoSuchMethodException var5) {
         throw new ReflectUtil.ReflectException(var5);
      }
   }

   private static Method getDeclaredMethod(Class<?> clazz, String name, boolean isStatic, boolean setAccessible, Class<?> rType, Class... pTypes) {
      Method method = lookupMethod(clazz, name, pTypes);
      if (!rType.equals(Void.TYPE) && !rType.isAssignableFrom(method.getReturnType())) {
         throw new ReflectUtil.ReflectException("Class=" + clazz.getName() + " method=" + name + " type " + method.getReturnType().getName() + " not assignment-compatible with " + rType.getName());
      } else {
         int mods = method.getModifiers();
         if (Modifier.isStatic(mods) != isStatic) {
            throw new ReflectUtil.ReflectException("Class=" + clazz.getName() + " method=" + name + " expected isStatic=" + isStatic);
         } else {
            if (setAccessible) {
               method.setAccessible(true);
            }

            return method;
         }
      }
   }

   private static <T> Constructor<? extends T> getDeclaredConstructor(Class<? extends T> clazz, boolean setAccessible, Class... pTypes) {
      Constructor constructor;
      try {
         constructor = clazz.getDeclaredConstructor(pTypes);
      } catch (SecurityException | NoSuchMethodException var5) {
         throw new ReflectUtil.ReflectException(var5);
      }

      if (setAccessible) {
         constructor.setAccessible(true);
      }

      return constructor;
   }

   public static <C, R> Functions.R1<R, C> loadInstanceR0(Class<? super C> clazz, String name, boolean setAccessible, final Class<? extends R> rType) {
      final Method method = getDeclaredMethod(clazz, name, false, setAccessible, rType);
      return new Functions.R1<R, C>() {
         public R call(C instance) {
            return ReflectUtil.invoke(method, instance, rType);
         }
      };
   }

   public static <A1, A2, A3, A4, C, R> Functions.R5<R, C, A1, A2, A3, A4> loadInstanceR4(Class<? super C> clazz, String name, boolean setAccessible, final Class<? extends R> rType, Class<? super A1> a1Type, Class<? super A2> a2Type, Class<? super A3> a3Type, Class<? super A4> a4Type) {
      final Method method = getDeclaredMethod(clazz, name, false, setAccessible, rType, a1Type, a2Type, a3Type, a4Type);
      return new Functions.R5<R, C, A1, A2, A3, A4>() {
         public R call(C instance, A1 a1, A2 a2, A3 a3, A4 a4) {
            return ReflectUtil.invoke(method, instance, rType, a1, a2, a3, a4);
         }
      };
   }

   public static <A1, C> Functions.V2<C, A1> loadInstanceV1(Class<? super C> clazz, String name, boolean setAccessible, Class<? super A1> a1Type) {
      final Method method = getDeclaredMethod(clazz, name, false, setAccessible, Void.TYPE, a1Type);
      return new Functions.V2<C, A1>() {
         public void call(C instance, A1 a1) {
            ReflectUtil.invoke(method, instance, Void.TYPE, a1);
         }
      };
   }

   public static <C, R> Functions.R0<R> bindInstanceR0(final C instance, String name, boolean setAccessible, final Class<? extends R> rType) {
      final Method method = getDeclaredMethod(instance.getClass(), name, false, setAccessible, rType);
      return new Functions.R0<R>() {
         public R call() {
            return ReflectUtil.invoke(method, instance, rType);
         }
      };
   }

   public static <A1, C, R> Functions.R1<R, A1> bindInstanceR1(final C instance, String name, boolean setAccessible, final Class<? extends R> rType, Class<? super A1> a1Type) {
      final Method method = getDeclaredMethod(instance.getClass(), name, false, setAccessible, rType, a1Type);
      return new Functions.R1<R, A1>() {
         public R call(A1 a1) {
            return ReflectUtil.invoke(method, instance, rType, a1);
         }
      };
   }

   public static <A1, C> Functions.V1<A1> bindInstanceV1(final C instance, String name, boolean setAccessible, Class<? super A1> a1Type) {
      final Method method = getDeclaredMethod(instance.getClass(), name, false, setAccessible, Void.TYPE, a1Type);
      return new Functions.V1<A1>() {
         public void call(A1 a1) {
            ReflectUtil.invoke(method, instance, Void.TYPE, a1);
         }
      };
   }

   public static <A1, A2, C> Functions.V2<A1, A2> bindInstanceV2(final C instance, String name, boolean setAccessible, Class<? super A1> a1Type, Class<? super A2> a2Type) {
      final Method method = getDeclaredMethod(instance.getClass(), name, false, setAccessible, Void.TYPE, a1Type, a2Type);
      return new Functions.V2<A1, A2>() {
         public void call(A1 a1, A2 a2) {
            ReflectUtil.invoke(method, instance, Void.TYPE, a1, a2);
         }
      };
   }

   public static <R> Functions.R0<R> loadStaticR0(Class<?> clazz, String name, boolean setAccessible, final Class<? extends R> rType) {
      final Method method = getDeclaredMethod(clazz, name, true, setAccessible, rType);
      return new Functions.R0<R>() {
         public R call() {
            return ReflectUtil.invoke(method, (Object)null, rType);
         }
      };
   }

   public static <A1, R> Functions.R1<R, A1> loadStaticR1(Class<?> clazz, String name, boolean setAccessible, final Class<? extends R> rType, Class<? super A1> a1Type) {
      final Method method = getDeclaredMethod(clazz, name, true, setAccessible, rType, a1Type);
      return new Functions.R1<R, A1>() {
         public R call(A1 a1) {
            return ReflectUtil.invoke(method, (Object)null, rType, a1);
         }
      };
   }

   public static <A1, A2> Functions.V2<A1, A2> loadStaticV2(Class<?> clazz, String name, boolean setAccessible, Class<? super A1> a1Type, Class<? super A2> a2Type) {
      final Method method = getDeclaredMethod(clazz, name, true, setAccessible, Void.TYPE, a1Type, a2Type);
      return new Functions.V2<A1, A2>() {
         public void call(A1 a1, A2 a2) {
            ReflectUtil.invoke(method, (Object)null, Void.TYPE, a1, a2);
         }
      };
   }

   public static <C> Functions.R0<C> loadConstructor0(Class<? extends C> clazz, boolean setAccessible) {
      final Constructor<? extends C> constructor = getDeclaredConstructor(clazz, setAccessible);
      return new Functions.R0<C>() {
         public C call() {
            return ReflectUtil.newInstance(constructor);
         }
      };
   }

   public static <A1, C> Functions.R1<C, A1> loadConstructor1(Class<? extends C> clazz, boolean setAccessible, Class<? super A1> a1Type) {
      final Constructor<? extends C> constructor = getDeclaredConstructor(clazz, setAccessible, a1Type);
      return new Functions.R1<C, A1>() {
         public C call(A1 a1) {
            return ReflectUtil.newInstance(constructor, a1);
         }
      };
   }

   public static <A1, A2, C> Functions.R2<C, A1, A2> loadConstructor2(Class<? extends C> clazz, boolean setAccessible, Class<? super A1> a1Type, Class<? super A2> a2Type) {
      final Constructor<? extends C> constructor = getDeclaredConstructor(clazz, setAccessible, a1Type, a2Type);
      return new Functions.R2<C, A1, A2>() {
         public C call(A1 a1, A2 a2) {
            return ReflectUtil.newInstance(constructor, a1, a2);
         }
      };
   }

   public static <C, A1, A2, A3> Functions.R3<C, A1, A2, A3> loadConstuctor3(Class<? extends C> clazz, boolean setAccessible, Class<? super A1> a1Type, Class<? super A2> a2Type, Class<? super A3> a3Type) {
      final Constructor<? extends C> constructor = getDeclaredConstructor(clazz, setAccessible, a1Type, a2Type, a3Type);
      return new Functions.R3<C, A1, A2, A3>() {
         public C call(A1 a1, A2 a2, A3 a3) {
            return ReflectUtil.newInstance(constructor, a1, a2, a3);
         }
      };
   }

   public static <C, A1, A2, A3, A4> Functions.R4<C, A1, A2, A3, A4> loadConstuctor4(Class<? extends C> clazz, boolean setAccessible, Class<? super A1> a1Type, Class<? super A2> a2Type, Class<? super A3> a3Type, Class<? super A4> a4Type) {
      final Constructor<? extends C> constructor = getDeclaredConstructor(clazz, setAccessible, a1Type, a2Type, a3Type, a4Type);
      return new Functions.R4<C, A1, A2, A3, A4>() {
         public C call(A1 a1, A2 a2, A3 a3, A4 a4) {
            return ReflectUtil.newInstance(constructor, a1, a2, a3, a4);
         }
      };
   }

   public static <C, A1, A2, A3, A4, A5> Functions.R5<C, A1, A2, A3, A4, A5> loadConstuctor5(Class<? extends C> clazz, boolean setAccessible, Class<? super A1> a1Type, Class<? super A2> a2Type, Class<? super A3> a3Type, Class<? super A4> a4Type, Class<? super A5> a5Type) {
      final Constructor<? extends C> constructor = getDeclaredConstructor(clazz, setAccessible, a1Type, a2Type, a3Type, a4Type, a5Type);
      return new Functions.R5<C, A1, A2, A3, A4, A5>() {
         public C call(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5) {
            return ReflectUtil.newInstance(constructor, a1, a2, a3, a4, a5);
         }
      };
   }

   public static <C, A1, A2, A3, A4, A5, A6, A7, A8, A9> Functions.R9<C, A1, A2, A3, A4, A5, A6, A7, A8, A9> loadConstuctor9(Class<? extends C> clazz, boolean setAccessible, Class<? super A1> a1Type, Class<? super A2> a2Type, Class<? super A3> a3Type, Class<? super A4> a4Type, Class<? super A5> a5Type, Class<? super A6> a6Type, Class<? super A7> a7Type, Class<? super A8> a8Type, Class<? super A9> a9type) {
      final Constructor<? extends C> constructor = getDeclaredConstructor(clazz, setAccessible, a1Type, a2Type, a3Type, a4Type, a5Type, a6Type, a7Type, a8Type, a9type);
      return new Functions.R9<C, A1, A2, A3, A4, A5, A6, A7, A8, A9>() {
         public C call(A1 a1, A2 a2, A3 a3, A4 a4, A5 a5, A6 a6, A7 a7, A8 a8, A9 a9) {
            return ReflectUtil.newInstance(constructor, a1, a2, a3, a4, a5, a6, a7, a8, a9);
         }
      };
   }

   public static <T> T getStaticField(Class<?> clazz, String name, Class<? extends T> type) {
      Functions.R1 caster = makeCaster(type);

      try {
         return caster.call(clazz.getField(name).get((Object)null));
      } catch (SecurityException | IllegalAccessException | NoSuchFieldException var5) {
         throw new ReflectUtil.ReflectException(var5);
      }
   }

   public static void setStaticField(Class<?> clazz, String name, boolean setAccessible, Object value) {
      try {
         Field field = clazz.getDeclaredField(name);
         if (setAccessible) {
            field.setAccessible(true);
         }

         field.set((Object)null, value);
      } catch (SecurityException | IllegalAccessException | NoSuchFieldException var5) {
         throw new ReflectUtil.ReflectException(var5);
      }
   }

   public static class ReflectException extends RuntimeException {
      private static final long serialVersionUID = 1L;

      public ReflectException() {
      }

      public ReflectException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
         super(message, cause, enableSuppression, writableStackTrace);
      }

      public ReflectException(String message, Throwable cause) {
         super(message, cause);
      }

      public ReflectException(String message) {
         super(message);
      }

      public ReflectException(Throwable cause) {
         super(cause);
      }
   }
}
