package lambdainternal.events.mixins;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public abstract class CodeCommitEventMixin {
   public CodeCommitEventMixin() {
   }

   @JsonProperty("Records")
   abstract List<?> getRecords();

   @JsonProperty("Records")
   abstract void setRecords(List<?> var1);

   public abstract class RecordMixin {
      public RecordMixin() {
      }

      @JsonProperty("codecommit")
      abstract Object getCodeCommit();

      @JsonProperty("codecommit")
      abstract void setCodeCommit(Object var1);

      @JsonProperty("eventSourceARN")
      abstract String getEventSourceArn();

      @JsonProperty("eventSourceARN")
      abstract void setEventSourceArn(String var1);

      @JsonProperty("userIdentityARN")
      abstract String getUserIdentityArn();

      @JsonProperty("userIdentityARN")
      abstract void setUserIdentityArn(String var1);
   }
}
