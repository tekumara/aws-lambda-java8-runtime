package lambdainternal.serializerfactories;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;

public class JacksonFactory implements PojoSerializerFactory {
   private static final ObjectMapper globalMapper = createObjectMapper();
   private static final JacksonFactory instance;
   private final ObjectMapper mapper;

   public static JacksonFactory getInstance() {
      return instance;
   }

   private JacksonFactory(ObjectMapper mapper) {
      this.mapper = mapper;
   }

   public ObjectMapper getMapper() {
      return this.mapper;
   }

   private static ObjectMapper createObjectMapper() {
      ObjectMapper mapper = new ObjectMapper(createJsonFactory());
      SerializationConfig scfg = mapper.getSerializationConfig();
      scfg = scfg.withFeatures(new SerializationFeature[]{SerializationFeature.FAIL_ON_SELF_REFERENCES, SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, SerializationFeature.WRAP_EXCEPTIONS});
      scfg = scfg.withoutFeatures(new SerializationFeature[]{SerializationFeature.CLOSE_CLOSEABLE, SerializationFeature.EAGER_SERIALIZER_FETCH, SerializationFeature.FAIL_ON_EMPTY_BEANS, SerializationFeature.FLUSH_AFTER_WRITE_VALUE, SerializationFeature.INDENT_OUTPUT, SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID, SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS, SerializationFeature.WRAP_ROOT_VALUE});
      mapper.setConfig(scfg);
      DeserializationConfig dcfg = mapper.getDeserializationConfig();
      dcfg = dcfg.withFeatures(new DeserializationFeature[]{DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS, DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS, DeserializationFeature.WRAP_EXCEPTIONS});
      dcfg = dcfg.withoutFeatures(new DeserializationFeature[]{DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES});
      mapper.setConfig(dcfg);
      mapper.setSerializationInclusion(Include.NON_NULL);
      mapper.enable(new MapperFeature[]{MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS});
      mapper.enable(new MapperFeature[]{MapperFeature.AUTO_DETECT_FIELDS});
      mapper.enable(new MapperFeature[]{MapperFeature.AUTO_DETECT_GETTERS});
      mapper.enable(new MapperFeature[]{MapperFeature.AUTO_DETECT_IS_GETTERS});
      mapper.enable(new MapperFeature[]{MapperFeature.AUTO_DETECT_SETTERS});
      mapper.enable(new MapperFeature[]{MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS});
      mapper.enable(new MapperFeature[]{MapperFeature.USE_STD_BEAN_NAMING});
      mapper.enable(new MapperFeature[]{MapperFeature.USE_ANNOTATIONS});
      mapper.disable(new MapperFeature[]{MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES});
      mapper.disable(new MapperFeature[]{MapperFeature.AUTO_DETECT_CREATORS});
      mapper.disable(new MapperFeature[]{MapperFeature.INFER_PROPERTY_MUTATORS});
      mapper.disable(new MapperFeature[]{MapperFeature.SORT_PROPERTIES_ALPHABETICALLY});
      mapper.disable(new MapperFeature[]{MapperFeature.USE_GETTERS_AS_SETTERS});
      mapper.disable(new MapperFeature[]{MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME});
      mapper.disable(new MapperFeature[]{MapperFeature.USE_STATIC_TYPING});
      mapper.disable(new MapperFeature[]{MapperFeature.REQUIRE_SETTERS_FOR_GETTERS});
      return mapper;
   }

   private static JsonFactory createJsonFactory() {
      JsonFactory factory = new JsonFactory();
      factory.enable(Feature.ALLOW_NON_NUMERIC_NUMBERS);
      factory.enable(Feature.ALLOW_NUMERIC_LEADING_ZEROS);
      factory.enable(Feature.ALLOW_SINGLE_QUOTES);
      factory.enable(Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
      factory.enable(Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
      factory.enable(Feature.ALLOW_UNQUOTED_FIELD_NAMES);
      factory.disable(Feature.ALLOW_COMMENTS);
      factory.disable(Feature.ALLOW_YAML_COMMENTS);
      factory.disable(Feature.AUTO_CLOSE_SOURCE);
      factory.disable(Feature.STRICT_DUPLICATE_DETECTION);
      factory.enable(com.fasterxml.jackson.core.JsonGenerator.Feature.IGNORE_UNKNOWN);
      factory.enable(com.fasterxml.jackson.core.JsonGenerator.Feature.QUOTE_FIELD_NAMES);
      factory.enable(com.fasterxml.jackson.core.JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS);
      factory.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
      factory.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_TARGET);
      factory.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.ESCAPE_NON_ASCII);
      factory.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM);
      factory.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION);
      factory.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
      factory.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
      return factory;
   }

   public <T> PojoSerializerFactory.PojoSerializer<T> getSerializer(Class<T> clazz) {
      return new JacksonFactory.ClassSerializer(this.mapper, clazz);
   }

   public PojoSerializerFactory.PojoSerializer<Object> getSerializer(Type type) {
      return new JacksonFactory.TypeSerializer(this.mapper, type);
   }

   public JacksonFactory withNamingStrategy(PropertyNamingStrategy strategy) {
      return new JacksonFactory(this.mapper.copy().setPropertyNamingStrategy(strategy));
   }

   public JacksonFactory withMixin(Class<?> clazz, Class<?> mixin) {
      return new JacksonFactory(this.mapper.copy().addMixIn(clazz, mixin));
   }

   static {
      instance = new JacksonFactory(globalMapper);
   }

   private static final class ClassSerializer<T> extends JacksonFactory.InternalSerializer<T> {
      public ClassSerializer(ObjectMapper mapper, Class<T> clazz) {
         super(mapper.reader(clazz), mapper.writerFor(clazz));
      }
   }

   public static final class TypeSerializer extends JacksonFactory.InternalSerializer<Object> {
      public TypeSerializer(ObjectMapper mapper, JavaType type) {
         super(mapper.reader(type), mapper.writerFor(type));
      }

      public TypeSerializer(ObjectMapper mapper, Type type) {
         this(mapper, mapper.constructType(type));
      }
   }

   private static class InternalSerializer<T> implements PojoSerializerFactory.PojoSerializer<T> {
      private final ObjectReader reader;
      private final ObjectWriter writer;

      public InternalSerializer(ObjectReader reader, ObjectWriter writer) {
         this.reader = reader;
         this.writer = writer;
      }

      public T fromJson(InputStream input) {
         try {
            return this.reader.readValue(input);
         } catch (IOException var3) {
            throw new UncheckedIOException(var3);
         }
      }

      public T fromJson(String input) {
         try {
            return this.reader.readValue(input);
         } catch (IOException var3) {
            throw new UncheckedIOException(var3);
         }
      }

      public void toJson(T value, OutputStream output) {
         try {
            this.writer.writeValue(output, value);
         } catch (IOException var4) {
            throw new UncheckedIOException(var4);
         }
      }
   }
}
