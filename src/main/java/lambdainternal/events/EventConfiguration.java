package lambdainternal.events;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.PascalCaseStrategy;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lambdainternal.events.mixins.CloudFrontEventMixin;
import lambdainternal.events.mixins.CloudWatchLogsEventMixin;
import lambdainternal.events.mixins.CodeCommitEventMixin;
import lambdainternal.events.mixins.DynamodbEventMixin;
import lambdainternal.events.mixins.KinesisEventMixin;
import lambdainternal.events.mixins.SNSEventMixin;
import lambdainternal.events.mixins.SQSEventMixin;
import lambdainternal.events.mixins.ScheduledEventMixin;
import lambdainternal.events.modules.DateModule;
import lambdainternal.events.modules.DateTimeModule;
import lambdainternal.events.serializers.OrgJsonSerializer;
import lambdainternal.events.serializers.S3EventSerializer;
import lambdainternal.serializerfactories.JacksonFactory;
import lambdainternal.serializerfactories.PojoSerializerFactory;
import lambdainternal.util.SerializeUtil;

public class EventConfiguration {
   private static final List<String> SUPPORTED_EVENTS = (List)Stream.of("com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent", "com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent", "com.amazonaws.services.lambda.runtime.events.CloudFrontEvent", "com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent", "com.amazonaws.services.lambda.runtime.events.CodeCommitEvent", "com.amazonaws.services.lambda.runtime.events.CognitoEvent", "com.amazonaws.services.lambda.runtime.events.ConfigEvent", "com.amazonaws.services.lambda.runtime.events.DynamodbEvent", "com.amazonaws.services.lambda.runtime.events.IoTButtonEvent", "com.amazonaws.services.lambda.runtime.events.KinesisEvent", "com.amazonaws.services.lambda.runtime.events.KinesisFirehoseEvent", "com.amazonaws.services.lambda.runtime.events.LexEvent", "com.amazonaws.services.lambda.runtime.events.ScheduledEvent", "com.amazonaws.services.s3.event.S3EventNotification", "com.amazonaws.services.lambda.runtime.events.S3Event", "com.amazonaws.services.lambda.runtime.events.SNSEvent", "com.amazonaws.services.lambda.runtime.events.SQSEvent").collect(Collectors.toList());
   private static final Map<String, OrgJsonSerializer> SERIALIZER_MAP = (Map)Stream.of(new SimpleEntry("com.amazonaws.services.s3.event.S3EventNotification", new S3EventSerializer()), new SimpleEntry("com.amazonaws.services.lambda.runtime.events.S3Event", new S3EventSerializer())).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
   private static final Map<String, Class> MIXIN_MAP = (Map)Stream.of(new SimpleEntry("com.amazonaws.services.lambda.runtime.events.CloudFrontEvent", CloudFrontEventMixin.class), new SimpleEntry("com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent", CloudWatchLogsEventMixin.class), new SimpleEntry("com.amazonaws.services.lambda.runtime.events.CodeCommitEvent", CodeCommitEventMixin.class), new SimpleEntry("com.amazonaws.services.lambda.runtime.events.CodeCommitEvent$Record", CodeCommitEventMixin.RecordMixin.class), new SimpleEntry("com.amazonaws.services.lambda.runtime.events.DynamodbEvent", DynamodbEventMixin.class), new SimpleEntry("com.amazonaws.services.lambda.runtime.events.DynamodbEvent$DynamodbStreamRecord", DynamodbEventMixin.DynamodbStreamRecordMixin.class), new SimpleEntry("com.amazonaws.services.dynamodbv2.model.StreamRecord", DynamodbEventMixin.StreamRecordMixin.class), new SimpleEntry("com.amazonaws.services.dynamodbv2.model.AttributeValue", DynamodbEventMixin.AttributeValueMixin.class), new SimpleEntry("com.amazonaws.services.lambda.runtime.events.KinesisEvent", KinesisEventMixin.class), new SimpleEntry("com.amazonaws.services.lambda.runtime.events.KinesisEvent$Record", KinesisEventMixin.RecordMixin.class), new SimpleEntry("com.amazonaws.services.lambda.runtime.events.ScheduledEvent", ScheduledEventMixin.class), new SimpleEntry("com.amazonaws.services.lambda.runtime.events.SNSEvent", SNSEventMixin.class), new SimpleEntry("com.amazonaws.services.lambda.runtime.events.SNSEvent$SNSRecord", SNSEventMixin.SNSRecordMixin.class), new SimpleEntry("com.amazonaws.services.lambda.runtime.events.SQSEvent", SQSEventMixin.class), new SimpleEntry("com.amazonaws.services.lambda.runtime.events.SQSEvent$SQSMessage", SQSEventMixin.SQSMessageMixin.class)).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
   private static final Map<String, List<String>> NESTED_CLASS_MAP = (Map)Stream.of(new SimpleEntry("com.amazonaws.services.lambda.runtime.events.CodeCommitEvent", Arrays.asList("com.amazonaws.services.lambda.runtime.events.CodeCommitEvent$Record")), new SimpleEntry("com.amazonaws.services.lambda.runtime.events.CognitoEvent", Arrays.asList("com.amazonaws.services.lambda.runtime.events.CognitoEvent$DatasetRecord")), new SimpleEntry("com.amazonaws.services.lambda.runtime.events.DynamodbEvent", Arrays.asList("com.amazonaws.services.dynamodbv2.model.AttributeValue", "com.amazonaws.services.dynamodbv2.model.StreamRecord", "com.amazonaws.services.lambda.runtime.events.DynamodbEvent$DynamodbStreamRecord")), new SimpleEntry("com.amazonaws.services.lambda.runtime.events.KinesisEvent", Arrays.asList("com.amazonaws.services.lambda.runtime.events.KinesisEvent$Record")), new SimpleEntry("com.amazonaws.services.lambda.runtime.events.SNSEvent", Arrays.asList("com.amazonaws.services.lambda.runtime.events.SNSEvent$SNSRecord")), new SimpleEntry("com.amazonaws.services.lambda.runtime.events.SQSEvent", Arrays.asList("com.amazonaws.services.lambda.runtime.events.SQSEvent$SQSMessage"))).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
   private static final Map<String, PropertyNamingStrategy> NAMING_STRATEGY_MAP = (Map)Stream.of(new SimpleEntry("com.amazonaws.services.lambda.runtime.events.SNSEvent", new PascalCaseStrategy())).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

   public EventConfiguration() {
   }

   public static boolean isLambdaSupportedEvent(String className) {
      return SUPPORTED_EVENTS.contains(className);
   }

   public static <T> PojoSerializerFactory.PojoSerializer<T> getEventSerializerFor(Class<T> eventClass) {
      if (SERIALIZER_MAP.containsKey(eventClass.getName())) {
         return ((OrgJsonSerializer)SERIALIZER_MAP.get(eventClass.getName())).withClass(eventClass);
      } else {
         JacksonFactory factory = JacksonFactory.getInstance();
         if (MIXIN_MAP.containsKey(eventClass.getName())) {
            factory = factory.withMixin(eventClass, (Class)MIXIN_MAP.get(eventClass.getName()));
         }

         if (NESTED_CLASS_MAP.containsKey(eventClass.getName())) {
            List<String> nestedClassNames = (List)NESTED_CLASS_MAP.get(eventClass.getName());
            Iterator var3 = nestedClassNames.iterator();

            while(var3.hasNext()) {
               String nestedClassName = (String)var3.next();
               if (MIXIN_MAP.containsKey(nestedClassName)) {
                  Class<?> eventClazz = SerializeUtil.loadCustomerClass(nestedClassName);
                  Class<?> mixinClazz = (Class)MIXIN_MAP.get(nestedClassName);
                  factory = factory.withMixin(eventClazz, mixinClazz);
               }
            }
         }

         factory.getMapper().registerModules(new Module[]{new DateModule(), new DateTimeModule()});
         if (NAMING_STRATEGY_MAP.containsKey(eventClass.getName())) {
            factory = factory.withNamingStrategy((PropertyNamingStrategy)NAMING_STRATEGY_MAP.get(eventClass.getName()));
         }

         return factory.getSerializer(eventClass);
      }
   }
}
