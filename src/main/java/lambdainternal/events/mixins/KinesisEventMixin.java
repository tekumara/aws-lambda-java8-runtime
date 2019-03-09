package lambdainternal.events.mixins;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public abstract class KinesisEventMixin {
   public KinesisEventMixin() {
   }

   @JsonProperty("Records")
   abstract List<?> getRecords();

   @JsonProperty("Records")
   abstract void setRecords(List<?> var1);

   public abstract class RecordMixin {
      public RecordMixin() {
      }

      @JsonProperty("encryptionType")
      abstract String getEncryptionType();

      @JsonProperty("encryptionType")
      abstract void setEncryptionType(String var1);
   }
}
