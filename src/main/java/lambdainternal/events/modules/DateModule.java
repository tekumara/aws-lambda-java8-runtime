package lambdainternal.events.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.util.Date;

public class DateModule extends SimpleModule {
   private static final long serialVersionUID = 1L;

   private static double millisToSeconds(double millis) {
      return millis / 1000.0D;
   }

   private static double secondsToMillis(double seconds) {
      return seconds * 1000.0D;
   }

   public DateModule() {
      super(PackageVersion.VERSION);
      this.addSerializer(Date.class, new DateModule.Serializer());
      this.addDeserializer(Date.class, new DateModule.Deserializer());
   }

   public static final class Deserializer extends JsonDeserializer<Date> {
      public Deserializer() {
      }

      public Date deserialize(JsonParser parser, DeserializationContext context) throws IOException {
         double dateSeconds = parser.getValueAsDouble();
         return dateSeconds == 0.0D ? null : new Date((long)DateModule.secondsToMillis(dateSeconds));
      }
   }

   public static final class Serializer extends JsonSerializer<Date> {
      public Serializer() {
      }

      public void serialize(Date date, JsonGenerator generator, SerializerProvider serializers) throws IOException {
         if (date != null) {
            generator.writeNumber(DateModule.millisToSeconds((double)date.getTime()));
         }

      }
   }
}
