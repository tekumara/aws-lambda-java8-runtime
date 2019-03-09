package lambdainternal.serializerfactories;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

public interface PojoSerializerFactory {
   <T> PojoSerializerFactory.PojoSerializer<T> getSerializer(Class<T> var1);

   PojoSerializerFactory.PojoSerializer<Object> getSerializer(Type var1);

   public interface PojoSerializer<T> {
      T fromJson(InputStream var1);

      T fromJson(String var1);

      void toJson(T var1, OutputStream var2);
   }
}
