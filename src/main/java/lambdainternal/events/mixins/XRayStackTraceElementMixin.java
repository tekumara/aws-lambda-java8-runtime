package lambdainternal.events.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class XRayStackTraceElementMixin {
   public XRayStackTraceElementMixin() {
   }

   @JsonProperty("path")
   abstract String getFileName();

   @JsonProperty("line")
   abstract String getLineNumber();

   @JsonProperty("label")
   abstract String getMethodName();

   @JsonIgnore
   abstract String getClassName();

   @JsonIgnore
   abstract boolean isNativeMethod();
}
