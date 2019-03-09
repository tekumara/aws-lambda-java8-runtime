package lambdainternal.events.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import lambdainternal.util.SerializeUtil;

public class DateTimeModule extends SimpleModule {
   public DateTimeModule() {
      super(PackageVersion.VERSION);
      Class dateTimeClass = SerializeUtil.loadCustomerClass("org.joda.time.DateTime");
      this.addSerializer(dateTimeClass, this.getSerializer(dateTimeClass));
      this.addDeserializer(dateTimeClass, this.getDeserializer(dateTimeClass));
   }

   private <T> JsonSerializer<T> getSerializer(Class<T> dateTimeClass) {
      return new JsonSerializer<T>() {
         public void serialize(T dateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
            jsonGenerator.writeString(SerializeUtil.serializeDateTime(dateTime));
         }
      };
   }

   private <T> JsonDeserializer<T> getDeserializer(final Class<T> dateTimeClass) {
      return new JsonDeserializer<T>() {
         public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return SerializeUtil.deserializeDateTime(dateTimeClass, jsonParser.getValueAsString());
         }
      };
   }
}
