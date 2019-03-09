package lambdainternal.events.mixins;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ScheduledEventMixin {
   public ScheduledEventMixin() {
   }

   @JsonProperty("detail-type")
   abstract String getDetailType();

   @JsonProperty("detail-type")
   abstract void setDetailType(String var1);
}
