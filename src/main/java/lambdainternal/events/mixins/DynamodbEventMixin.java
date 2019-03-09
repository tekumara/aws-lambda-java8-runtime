package lambdainternal.events.mixins;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class DynamodbEventMixin {
   public DynamodbEventMixin() {
   }

   @JsonProperty("Records")
   abstract List<?> getRecords();

   @JsonProperty("Records")
   abstract void setRecords(List<?> var1);

   public abstract class AttributeValueMixin {
      public AttributeValueMixin() {
      }

      @JsonProperty("S")
      abstract String getS();

      @JsonProperty("S")
      abstract void setS(String var1);

      @JsonProperty("N")
      abstract String getN();

      @JsonProperty("N")
      abstract void setN(String var1);

      @JsonProperty("B")
      abstract ByteBuffer getB();

      @JsonProperty("B")
      abstract void setB(ByteBuffer var1);

      @JsonProperty("NULL")
      abstract Boolean isNULL();

      @JsonProperty("NULL")
      abstract void setNULL(Boolean var1);

      @JsonProperty("BOOL")
      abstract Boolean getBOOL();

      @JsonProperty("BOOL")
      abstract void setBOOL(Boolean var1);

      @JsonProperty("SS")
      abstract List<String> getSS();

      @JsonProperty("SS")
      abstract void setSS(List<String> var1);

      @JsonProperty("NS")
      abstract List<String> getNS();

      @JsonProperty("NS")
      abstract void setNS(List<String> var1);

      @JsonProperty("BS")
      abstract List<String> getBS();

      @JsonProperty("BS")
      abstract void setBS(List<String> var1);

      @JsonProperty("M")
      abstract Map<String, ?> getM();

      @JsonProperty("M")
      abstract void setM(Map<String, ?> var1);

      @JsonProperty("L")
      abstract List<?> getL();

      @JsonProperty("L")
      abstract void setL(List<?> var1);
   }

   public abstract class StreamRecordMixin {
      public StreamRecordMixin() {
      }

      @JsonProperty("Keys")
      abstract Map<String, ?> getKeys();

      @JsonProperty("Keys")
      abstract void setKeys(Map<String, ?> var1);

      @JsonProperty("SizeBytes")
      abstract Long getSizeBytes();

      @JsonProperty("SizeBytes")
      abstract void setSizeBytes(Long var1);

      @JsonProperty("SequenceNumber")
      abstract String getSequenceNumber();

      @JsonProperty("SequenceNumber")
      abstract void setSequenceNumber(String var1);

      @JsonProperty("StreamViewType")
      abstract String getStreamViewType();

      @JsonProperty("StreamViewType")
      abstract void setStreamViewType(String var1);

      @JsonProperty("NewImage")
      abstract Map<String, ?> getNewImage();

      @JsonProperty("NewImage")
      abstract void setNewImage(Map<String, ?> var1);

      @JsonProperty("OldImage")
      abstract Map<String, ?> getOldImage();

      @JsonProperty("OldImage")
      abstract void setOldImage(Map<String, ?> var1);

      @JsonProperty("ApproximateCreationDateTime")
      abstract Date getApproximateCreationDateTime();

      @JsonProperty("ApproximateCreationDateTime")
      abstract void setApproximateCreationDateTime(Date var1);
   }

   public abstract class DynamodbStreamRecordMixin {
      public DynamodbStreamRecordMixin() {
      }

      @JsonProperty("eventName")
      abstract String getEventName();

      @JsonProperty("eventName")
      abstract void setEventName(String var1);

      @JsonProperty("eventSourceARN")
      abstract String getEventSourceArn();

      @JsonProperty("eventSourceARN")
      abstract void setEventSourceArn(String var1);
   }
}
