package lambdainternal.api;

import com.amazonaws.services.lambda.runtime.Client;
import com.amazonaws.services.lambda.runtime.ClientContext;
import java.util.Map;

public class LambdaClientContext implements ClientContext {
   private LambdaClientContextClient client;
   private Map<String, String> custom;
   private Map<String, String> env;

   public LambdaClientContext() {
   }

   public Client getClient() {
      return this.client;
   }

   public Map<String, String> getCustom() {
      return this.custom;
   }

   public Map<String, String> getEnvironment() {
      return this.env;
   }
}
