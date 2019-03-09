package lambdainternal.events.mixins;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class CloudWatchLogsEventMixin {
   public CloudWatchLogsEventMixin() {
   }

   @JsonProperty("awslogs")
   abstract Object getAwsLogs();

   @JsonProperty("awslogs")
   abstract void setAwsLogs(Object var1);
}
