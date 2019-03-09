package lambdainternal.api;

import com.amazonaws.services.lambda.runtime.CognitoIdentity;

public class LambdaCognitoIdentity implements CognitoIdentity {
   private final String identityId;
   private final String poolId;

   public LambdaCognitoIdentity(String identityid, String poolid) {
      this.identityId = identityid;
      this.poolId = poolid;
   }

   public String getIdentityId() {
      return this.identityId;
   }

   public String getIdentityPoolId() {
      return this.poolId;
   }
}
