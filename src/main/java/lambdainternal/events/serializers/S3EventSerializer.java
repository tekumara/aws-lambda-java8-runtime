package lambdainternal.events.serializers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lambdainternal.util.Functions;
import lambdainternal.util.ReflectUtil;
import lambdainternal.util.SerializeUtil;
import org.json.JSONArray;
import org.json.JSONObject;

public class S3EventSerializer<T> implements OrgJsonSerializer<T> {
   private Class<T> eventClass;

   public S3EventSerializer() {
   }

   public S3EventSerializer<T> withClass(Class<T> eventClass) {
      this.eventClass = eventClass;
      return this;
   }

   public T fromJson(InputStream input) {
      return this.fromJson(SerializeUtil.convertStreamToString(input));
   }

   public T fromJson(String input) {
      JSONObject jsonObject = new JSONObject(input);
      return this.deserializeEvent(jsonObject);
   }

   public void toJson(T value, OutputStream output) {
      Class s3EventBaseClass = SerializeUtil.loadCustomerClass("com.amazonaws.services.s3.event.S3EventNotification");
      JSONObject jsonObject = this.serializeEvent(value, s3EventBaseClass);

      try {
         Writer writer = new OutputStreamWriter(output);
         Throwable var6 = null;

         try {
            writer.write(jsonObject.toString());
         } catch (Throwable var16) {
            var6 = var16;
            throw var16;
         } finally {
            if (writer != null) {
               if (var6 != null) {
                  try {
                     writer.close();
                  } catch (Throwable var15) {
                     var6.addSuppressed(var15);
                  }
               } else {
                  writer.close();
               }
            }

         }

      } catch (IOException var18) {
         throw new UncheckedIOException(var18);
      }
   }

   private <A> JSONObject serializeEvent(T value, Class<A> s3EventBaseClass) {
      Class eventNotificationRecordClass = SerializeUtil.loadCustomerClass("com.amazonaws.services.s3.event.S3EventNotification$S3EventNotificationRecord");
      JSONObject jsonObject = new JSONObject();
      Functions.R0<List> getRecordsMethod = ReflectUtil.bindInstanceR0(value, "getRecords", true, List.class);
      jsonObject.put("Records", this.serializeEventNotificationRecordList((List)getRecordsMethod.call(), eventNotificationRecordClass));
      return jsonObject;
   }

   private T deserializeEvent(JSONObject jsonObject) {
      Class eventNotificationRecordClass = SerializeUtil.loadCustomerClass("com.amazonaws.services.s3.event.S3EventNotification$S3EventNotificationRecord");
      Functions.R1<T, List> constructor = ReflectUtil.loadConstructor1(this.eventClass, true, List.class);
      return constructor.call(this.deserializeEventNotificationRecordList(jsonObject.optJSONArray("Records"), eventNotificationRecordClass));
   }

   private <A> JSONArray serializeEventNotificationRecordList(List eventNotificationRecords, Class<A> eventNotificationRecordClass) {
      JSONArray jsonRecords = new JSONArray();
      Iterator var4 = eventNotificationRecords.iterator();

      while(var4.hasNext()) {
         Object eventNotificationRecord = var4.next();
         jsonRecords.put(this.serializeEventNotificationRecord(eventNotificationRecord));
      }

      return jsonRecords;
   }

   private <A> List<A> deserializeEventNotificationRecordList(JSONArray jsonRecords, Class<A> eventNotificiationRecordClass) {
      if (jsonRecords == null) {
         jsonRecords = new JSONArray();
      }

      Class s3EntityClass = SerializeUtil.loadCustomerClass("com.amazonaws.services.s3.event.S3EventNotification$S3Entity");
      Class s3BucketClass = SerializeUtil.loadCustomerClass("com.amazonaws.services.s3.event.S3EventNotification$S3BucketEntity");
      Class s3ObjectClass = SerializeUtil.loadCustomerClass("com.amazonaws.services.s3.event.S3EventNotification$S3ObjectEntity");
      Class requestParametersClass = SerializeUtil.loadCustomerClass("com.amazonaws.services.s3.event.S3EventNotification$RequestParametersEntity");
      Class responseElementsClass = SerializeUtil.loadCustomerClass("com.amazonaws.services.s3.event.S3EventNotification$ResponseElementsEntity");
      Class userIdentityClass = SerializeUtil.loadCustomerClass("com.amazonaws.services.s3.event.S3EventNotification$UserIdentityEntity");
      List<A> records = new ArrayList();

      for(int i = 0; i < jsonRecords.length(); ++i) {
         records.add(this.deserializeEventNotificationRecord(jsonRecords.getJSONObject(i), eventNotificiationRecordClass, s3EntityClass, s3BucketClass, s3ObjectClass, requestParametersClass, responseElementsClass, userIdentityClass));
      }

      return records;
   }

   private <A> JSONObject serializeEventNotificationRecord(A eventNotificationRecord) {
      Class s3EntityClass = SerializeUtil.loadCustomerClass("com.amazonaws.services.s3.event.S3EventNotification$S3Entity");
      Class s3BucketClass = SerializeUtil.loadCustomerClass("com.amazonaws.services.s3.event.S3EventNotification$S3BucketEntity");
      Class s3ObjectClass = SerializeUtil.loadCustomerClass("com.amazonaws.services.s3.event.S3EventNotification$S3ObjectEntity");
      Class requestParametersClass = SerializeUtil.loadCustomerClass("com.amazonaws.services.s3.event.S3EventNotification$RequestParametersEntity");
      Class responseElementsClass = SerializeUtil.loadCustomerClass("com.amazonaws.services.s3.event.S3EventNotification$ResponseElementsEntity");
      Class userIdentityClass = SerializeUtil.loadCustomerClass("com.amazonaws.services.s3.event.S3EventNotification$UserIdentityEntity");
      Class dateTimeClass = SerializeUtil.loadCustomerClass("org.joda.time.DateTime");
      JSONObject jsonObject = new JSONObject();
      Functions.R0<String> getAwsRegionMethod = ReflectUtil.bindInstanceR0(eventNotificationRecord, "getAwsRegion", true, String.class);
      jsonObject.put("awsRegion", getAwsRegionMethod.call());
      Functions.R0<String> getEventNameMethod = ReflectUtil.bindInstanceR0(eventNotificationRecord, "getEventName", true, String.class);
      jsonObject.put("eventName", getEventNameMethod.call());
      Functions.R0<String> getEventSourceMethod = ReflectUtil.bindInstanceR0(eventNotificationRecord, "getEventSource", true, String.class);
      jsonObject.put("eventSource", getEventSourceMethod.call());
      Functions.R0<?> getEventTimeMethod = ReflectUtil.bindInstanceR0(eventNotificationRecord, "getEventTime", true, dateTimeClass);
      jsonObject.put("eventTime", SerializeUtil.serializeDateTime(getEventTimeMethod.call()));
      Functions.R0<String> getEventVersionMethod = ReflectUtil.bindInstanceR0(eventNotificationRecord, "getEventVersion", true, String.class);
      jsonObject.put("eventVersion", getEventVersionMethod.call());
      Functions.R0<?> getRequestParametersMethod = ReflectUtil.bindInstanceR0(eventNotificationRecord, "getRequestParameters", true, requestParametersClass);
      jsonObject.put("requestParameters", this.serializeRequestParameters(getRequestParametersMethod.call()));
      Functions.R0<?> getResponseElementsMethod = ReflectUtil.bindInstanceR0(eventNotificationRecord, "getResponseElements", true, responseElementsClass);
      jsonObject.put("responseElements", this.serializeResponseElements(getResponseElementsMethod.call()));
      Functions.R0<?> getS3EntityMethod = ReflectUtil.bindInstanceR0(eventNotificationRecord, "getS3", true, s3EntityClass);
      jsonObject.put("s3", this.serializeS3Entity(getS3EntityMethod.call()));
      Functions.R0<?> getUserIdentityMethod = ReflectUtil.bindInstanceR0(eventNotificationRecord, "getUserIdentity", true, userIdentityClass);
      jsonObject.put("userIdentity", this.serializeUserIdentity(getUserIdentityMethod.call()));
      return jsonObject;
   }

   private <A, B, C, D, E, F, G> A deserializeEventNotificationRecord(JSONObject jsonObject, Class<A> eventNotificationRecordClass, Class<B> s3EntityClass, Class<C> s3BucketClass, Class<D> s3ObjectClass, Class<E> requestParametersClass, Class<F> responseElementsClass, Class<G> userIdentityClass) {
      if (jsonObject == null) {
         jsonObject = new JSONObject();
      }

      String awsRegion = jsonObject.optString("awsRegion");
      String eventName = jsonObject.optString("eventName");
      String eventSource = jsonObject.optString("eventSource");
      String eventTime = jsonObject.optString("eventTime");
      String eventVersion = jsonObject.optString("eventVersion");
      E requestParameters = this.deserializeRequestParameters(jsonObject.optJSONObject("requestParameters"), requestParametersClass);
      F responseElements = this.deserializeResponseElements(jsonObject.optJSONObject("responseElements"), responseElementsClass);
      B s3 = this.deserializeS3Entity(jsonObject.optJSONObject("s3"), s3EntityClass, s3BucketClass, s3ObjectClass, userIdentityClass);
      G userIdentity = this.deserializeUserIdentity(jsonObject.optJSONObject("userIdentity"), userIdentityClass);
      Functions.R9<A, String, String, String, String, String, E, F, B, G> constructor = ReflectUtil.loadConstuctor9(eventNotificationRecordClass, true, String.class, String.class, String.class, String.class, String.class, requestParametersClass, responseElementsClass, s3EntityClass, userIdentityClass);
      return constructor.call(awsRegion, eventName, eventSource, eventTime, eventVersion, requestParameters, responseElements, s3, userIdentity);
   }

   private <A> JSONObject serializeS3Entity(A s3Entity) {
      Class s3BucketClass = SerializeUtil.loadCustomerClass("com.amazonaws.services.s3.event.S3EventNotification$S3BucketEntity");
      Class s3ObjectClass = SerializeUtil.loadCustomerClass("com.amazonaws.services.s3.event.S3EventNotification$S3ObjectEntity");
      JSONObject jsonObject = new JSONObject();
      Functions.R0<String> getConfigurationIdMethod = ReflectUtil.bindInstanceR0(s3Entity, "getConfigurationId", true, String.class);
      jsonObject.put("configurationId", getConfigurationIdMethod.call());
      Functions.R0<?> getBucketMethod = ReflectUtil.bindInstanceR0(s3Entity, "getBucket", true, s3BucketClass);
      jsonObject.put("bucket", this.serializeS3Bucket(getBucketMethod.call()));
      Functions.R0<?> getObjectMethod = ReflectUtil.bindInstanceR0(s3Entity, "getObject", true, s3ObjectClass);
      jsonObject.put("object", this.serializeS3Object(getObjectMethod.call()));
      Functions.R0<String> getSchemaVersionMethod = ReflectUtil.bindInstanceR0(s3Entity, "getS3SchemaVersion", true, String.class);
      jsonObject.put("s3SchemaVersion", getSchemaVersionMethod.call());
      return jsonObject;
   }

   private <A, B, C, D> A deserializeS3Entity(JSONObject jsonObject, Class<A> s3EntityClass, Class<B> s3BucketClass, Class<C> s3ObjectClass, Class<D> userIdentityClass) {
      if (jsonObject == null) {
         jsonObject = new JSONObject();
      }

      String configurationId = jsonObject.optString("configurationId");
      B bucket = this.deserializeS3Bucket(jsonObject.optJSONObject("bucket"), s3BucketClass, userIdentityClass);
      C object = this.deserializeS3Object(jsonObject.optJSONObject("object"), s3ObjectClass);
      String schemaVersion = jsonObject.optString("s3SchemaVersion");
      Functions.R4<A, String, B, C, String> constructor = ReflectUtil.loadConstuctor4(s3EntityClass, true, String.class, s3BucketClass, s3ObjectClass, String.class);
      return constructor.call(configurationId, bucket, object, schemaVersion);
   }

   private <A> JSONObject serializeS3Bucket(A s3Bucket) {
      Class userIdentityClass = SerializeUtil.loadCustomerClass("com.amazonaws.services.s3.event.S3EventNotification$UserIdentityEntity");
      JSONObject jsonObject = new JSONObject();
      Functions.R0<String> getNameMethod = ReflectUtil.bindInstanceR0(s3Bucket, "getName", true, String.class);
      jsonObject.put("name", getNameMethod.call());
      Functions.R0<?> getOwnerIdentityMethod = ReflectUtil.bindInstanceR0(s3Bucket, "getOwnerIdentity", true, userIdentityClass);
      jsonObject.put("ownerIdentity", this.serializeUserIdentity(getOwnerIdentityMethod.call()));
      Functions.R0<String> getArnMethod = ReflectUtil.bindInstanceR0(s3Bucket, "getArn", true, String.class);
      jsonObject.put("arn", getArnMethod.call());
      return jsonObject;
   }

   private <A, B> A deserializeS3Bucket(JSONObject jsonObject, Class<A> s3BucketClass, Class<B> userIdentityClass) {
      if (jsonObject == null) {
         jsonObject = new JSONObject();
      }

      String name = jsonObject.optString("name");
      B ownerIdentity = this.deserializeUserIdentity(jsonObject.optJSONObject("ownerIdentity"), userIdentityClass);
      String arn = jsonObject.optString("arn");
      Functions.R3<A, String, B, String> constructor = ReflectUtil.loadConstuctor3(s3BucketClass, true, String.class, userIdentityClass, String.class);
      return constructor.call(name, ownerIdentity, arn);
   }

   private <A> JSONObject serializeS3Object(A s3Object) {
      JSONObject jsonObject = new JSONObject();
      Functions.R0<String> getKeyMethod = ReflectUtil.bindInstanceR0(s3Object, "getKey", true, String.class);
      jsonObject.put("key", getKeyMethod.call());
      Functions.R0<Long> getSizeMethod = ReflectUtil.bindInstanceR0(s3Object, "getSizeAsLong", true, Long.class);
      jsonObject.put("size", (Long)getSizeMethod.call());
      Functions.R0<String> getETagMethod = ReflectUtil.bindInstanceR0(s3Object, "geteTag", true, String.class);
      jsonObject.put("eTag", getETagMethod.call());
      Functions.R0<String> getVersionIdMethod = ReflectUtil.bindInstanceR0(s3Object, "getVersionId", true, String.class);
      jsonObject.put("versionId", getVersionIdMethod.call());

      try {
         Functions.R0<String> getUrlEncodedKeyMethod = ReflectUtil.bindInstanceR0(s3Object, "getUrlDecodedKey", true, String.class);
         jsonObject.put("urlDecodedKey", getUrlEncodedKeyMethod.call());
         Functions.R0<String> getSequencerMethod = ReflectUtil.bindInstanceR0(s3Object, "getSequencer", true, String.class);
         jsonObject.put("sequencer", getSequencerMethod.call());
      } catch (Exception var9) {
      }

      return jsonObject;
   }

   private <A> A deserializeS3Object(JSONObject jsonObject, Class<A> s3ObjectClass) {
      if (jsonObject == null) {
         jsonObject = new JSONObject();
      }

      String key = jsonObject.optString("key");
      Long size = jsonObject.optLong("size");
      String eTag = jsonObject.optString("eTag");
      String versionId = jsonObject.optString("versionId");
      String sequencer = jsonObject.optString("sequencer");

      try {
         Functions.R5<A, String, Long, String, String, String> constructor = ReflectUtil.loadConstuctor5(s3ObjectClass, true, String.class, Long.class, String.class, String.class, String.class);
         return constructor.call(key, size, eTag, versionId, sequencer);
      } catch (Exception var10) {
         Functions.R4<A, String, Long, String, String> constructor = ReflectUtil.loadConstuctor4(s3ObjectClass, true, String.class, Long.class, String.class, String.class);
         return constructor.call(key, size, eTag, versionId);
      }
   }

   private <A> JSONObject serializeUserIdentity(A userIdentity) {
      JSONObject jsonObject = new JSONObject();
      Functions.R0<String> getPrincipalIdMethod = ReflectUtil.bindInstanceR0(userIdentity, "getPrincipalId", true, String.class);
      jsonObject.put("principalId", getPrincipalIdMethod.call());
      return jsonObject;
   }

   private <A> A deserializeUserIdentity(JSONObject jsonObject, Class<A> userIdentityClass) {
      if (jsonObject == null) {
         jsonObject = new JSONObject();
      }

      String principalId = jsonObject.optString("principalId");
      Functions.R1<A, String> constructor = ReflectUtil.loadConstructor1(userIdentityClass, true, String.class);
      return constructor.call(principalId);
   }

   private <A> JSONObject serializeRequestParameters(A requestParameters) {
      JSONObject jsonObject = new JSONObject();
      Functions.R0<String> getSourceIpMethod = ReflectUtil.bindInstanceR0(requestParameters, "getSourceIPAddress", true, String.class);
      jsonObject.put("sourceIPAddress", getSourceIpMethod.call());
      return jsonObject;
   }

   private <A> A deserializeRequestParameters(JSONObject jsonObject, Class<A> requestParametersClass) {
      if (jsonObject == null) {
         jsonObject = new JSONObject();
      }

      String sourceIpAddress = jsonObject.optString("sourceIPAddress");
      Functions.R1<A, String> constructor = ReflectUtil.loadConstructor1(requestParametersClass, true, String.class);
      return constructor.call(sourceIpAddress);
   }

   private <A> JSONObject serializeResponseElements(A responseElements) {
      JSONObject jsonObject = new JSONObject();
      Functions.R0<String> getXAmzId2Method = ReflectUtil.bindInstanceR0(responseElements, "getxAmzId2", true, String.class);
      jsonObject.put("x-amz-id-2", getXAmzId2Method.call());
      Functions.R0<String> getXAmzRequestId = ReflectUtil.bindInstanceR0(responseElements, "getxAmzRequestId", true, String.class);
      jsonObject.put("x-amz-request-id", getXAmzRequestId.call());
      return jsonObject;
   }

   private <A> A deserializeResponseElements(JSONObject jsonObject, Class<A> responseElementsClass) {
      if (jsonObject == null) {
         jsonObject = new JSONObject();
      }

      String xAmzId2 = jsonObject.optString("x-amz-id-2");
      String xAmzRequestId = jsonObject.optString("x-amz-request-id");
      Functions.R2<A, String, String> constructor = ReflectUtil.loadConstructor2(responseElementsClass, true, String.class, String.class);
      return constructor.call(xAmzId2, xAmzRequestId);
   }
}
