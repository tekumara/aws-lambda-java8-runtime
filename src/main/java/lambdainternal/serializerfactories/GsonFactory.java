package lambdainternal.serializerfactories;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class GsonFactory implements PojoSerializerFactory {
   private static final Charset utf8;
   private static final Gson gson;
   private static final GsonFactory instance;

   public static GsonFactory getInstance() {
      return instance;
   }

   private GsonFactory() {
   }

   public <T> PojoSerializerFactory.PojoSerializer<T> getSerializer(Class<T> clazz) {
      return GsonFactory.InternalSerializer.create(clazz);
   }

   public PojoSerializerFactory.PojoSerializer<Object> getSerializer(Type type) {
      return GsonFactory.InternalSerializer.create(type);
   }

   static {
      utf8 = StandardCharsets.UTF_8;
      gson = (new GsonBuilder()).disableHtmlEscaping().serializeSpecialFloatingPointValues().create();
      instance = new GsonFactory();
   }

   private static class InternalSerializer<T> implements PojoSerializerFactory.PojoSerializer<T> {
      private final TypeAdapter<T> adapter;

      public InternalSerializer(TypeAdapter<T> adapter) {
         this.adapter = adapter.nullSafe();
      }

      public static <T> GsonFactory.InternalSerializer<T> create(TypeToken<T> token) {
         return Void.TYPE.equals(token.getRawType()) ? new GsonFactory.InternalSerializer(GsonFactory.gson.getAdapter(Object.class)) : new GsonFactory.InternalSerializer(GsonFactory.gson.getAdapter(token));
      }

      public static <T> GsonFactory.InternalSerializer<T> create(Class<T> clazz) {
         return create(TypeToken.get(clazz));
      }

      public static <T> GsonFactory.InternalSerializer<Object> create(Type type) {
         return create(TypeToken.get(type));
      }

      private T fromJson(JsonReader reader) {
         reader.setLenient(true);

         try {
            try {
               reader.peek();
            } catch (EOFException var3) {
               return null;
            }

            return this.adapter.read(reader);
         } catch (IOException var4) {
            throw new UncheckedIOException(var4);
         }
      }

      public T fromJson(InputStream input) {
         try {
            JsonReader reader = new JsonReader(new InputStreamReader(input, GsonFactory.utf8));
            Throwable var3 = null;

            Object var4;
            try {
               var4 = this.fromJson(reader);
            } catch (Throwable var14) {
               var3 = var14;
               throw var14;
            } finally {
               if (reader != null) {
                  if (var3 != null) {
                     try {
                        reader.close();
                     } catch (Throwable var13) {
                        var3.addSuppressed(var13);
                     }
                  } else {
                     reader.close();
                  }
               }

            }

            return var4;
         } catch (IOException var16) {
            throw new UncheckedIOException(var16);
         }
      }

      public T fromJson(String input) {
         try {
            JsonReader reader = new JsonReader(new StringReader(input));
            Throwable var3 = null;

            Object var4;
            try {
               var4 = this.fromJson(reader);
            } catch (Throwable var14) {
               var3 = var14;
               throw var14;
            } finally {
               if (reader != null) {
                  if (var3 != null) {
                     try {
                        reader.close();
                     } catch (Throwable var13) {
                        var3.addSuppressed(var13);
                     }
                  } else {
                     reader.close();
                  }
               }

            }

            return var4;
         } catch (IOException var16) {
            throw new UncheckedIOException(var16);
         }
      }

      public void toJson(T value, OutputStream output) {
         try {
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(output, GsonFactory.utf8));
            Throwable var4 = null;

            try {
               writer.setLenient(true);
               writer.setSerializeNulls(false);
               writer.setHtmlSafe(false);
               this.adapter.write(writer, value);
            } catch (Throwable var14) {
               var4 = var14;
               throw var14;
            } finally {
               if (writer != null) {
                  if (var4 != null) {
                     try {
                        writer.close();
                     } catch (Throwable var13) {
                        var4.addSuppressed(var13);
                     }
                  } else {
                     writer.close();
                  }
               }

            }

         } catch (IOException var16) {
            throw new UncheckedIOException(var16);
         }
      }
   }
}
