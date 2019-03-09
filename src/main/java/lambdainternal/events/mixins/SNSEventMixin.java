package lambdainternal.events.mixins;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public abstract class SNSEventMixin {
   public SNSEventMixin() {
   }

   @JsonProperty("Records")
   abstract List<?> getRecords();

   @JsonProperty("Records")
   abstract void setRecords(List<?> var1);

   public abstract class SNSRecordMixin {
      public SNSRecordMixin() {
      }

      @JsonProperty("Sns")
      abstract Object getSNS();

      @JsonProperty("Sns")
      abstract void setSns(Object var1);
   }
}
