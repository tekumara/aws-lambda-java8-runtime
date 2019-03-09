package lambdainternal.events.mixins;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public abstract class CloudFrontEventMixin {
   public CloudFrontEventMixin() {
   }

   @JsonProperty("Records")
   abstract List<?> getRecords();

   @JsonProperty("Records")
   abstract void setRecords(List<?> var1);
}
