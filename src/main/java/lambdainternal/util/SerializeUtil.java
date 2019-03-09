package lambdainternal.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;
import lambdainternal.AWSLambda;

public class SerializeUtil {
   private static final HashMap<String, Class> cachedClasses = new HashMap();

   public SerializeUtil() {
   }

   public static String convertStreamToString(InputStream inputStream) {
      Scanner s = (new Scanner(inputStream)).useDelimiter("\\A");
      return s.hasNext() ? s.next() : "";
   }

   public static Class loadCustomerClass(String className) {
      Class cachedClass = (Class)cachedClasses.get(className);
      if (cachedClass == null) {
         cachedClass = ReflectUtil.loadClass(AWSLambda.customerClassLoader, className);
         cachedClasses.put(className, cachedClass);
      }

      return cachedClass;
   }

   public static <T> T deserializeDateTime(Class<T> dateTimeClass, String dateTimeString) {
      Functions.R1<T, String> parseMethod = ReflectUtil.loadStaticR1(dateTimeClass, "parse", true, dateTimeClass, String.class);
      return parseMethod.call(dateTimeString);
   }

   public static <T> String serializeDateTime(T dateTime) {
      Class dateTimeFormatterClass = loadCustomerClass("org.joda.time.format.DateTimeFormatter");
      Class dateTimeFormatClass = loadCustomerClass("org.joda.time.format.ISODateTimeFormat");
      Class readableInstantInterface = loadCustomerClass("org.joda.time.ReadableInstant");
      return serializeDateTimeHelper(dateTime, dateTimeFormatterClass, dateTimeFormatClass, readableInstantInterface);
   }

   private static <S extends V, T, U, V> String serializeDateTimeHelper(S dateTime, Class<T> dateTimeFormatterClass, Class<U> dateTimeFormatClass, Class<V> readableInstantInterface) {
      Functions.R0<T> dateTimeFormatterConstructor = ReflectUtil.loadStaticR0(dateTimeFormatClass, "dateTime", true, dateTimeFormatterClass);
      T dateTimeFormatter = dateTimeFormatterConstructor.call();
      Functions.R1<String, S> printMethod = ReflectUtil.bindInstanceR1(dateTimeFormatter, "print", true, String.class, readableInstantInterface);
      return (String)printMethod.call(dateTime);
   }
}
