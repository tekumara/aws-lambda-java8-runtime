package lambdainternal.events.mixins;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public abstract class SQSEventMixin {
   public SQSEventMixin() {
   }

   @JsonProperty("Records")
   abstract List<?> getRecords();

   @JsonProperty("Records")
   abstract void setRecords(List<?> var1);

   public abstract class SQSMessageMixin {
      public SQSMessageMixin() {
      }

      @JsonProperty("eventSourceARN")
      abstract String getEventSourceArn();

      @JsonProperty("eventSourceARN")
      abstract void setEventSourceArn(String var1);
   }
}
