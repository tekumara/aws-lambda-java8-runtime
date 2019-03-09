package lambdainternal.events.serializers;

import java.io.InputStream;
import java.io.OutputStream;
import lambdainternal.serializerfactories.PojoSerializerFactory;

public interface OrgJsonSerializer<T> extends PojoSerializerFactory.PojoSerializer<T> {
   OrgJsonSerializer<T> withClass(Class<T> var1);

   T fromJson(InputStream var1);

   T fromJson(String var1);

   void toJson(T var1, OutputStream var2);
}
