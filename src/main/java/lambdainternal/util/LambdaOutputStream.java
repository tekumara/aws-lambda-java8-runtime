package lambdainternal.util;

import java.io.IOException;
import java.io.OutputStream;
import lambdainternal.LambdaRuntime;

public class LambdaOutputStream extends OutputStream {
   private final OutputStream inner;

   public LambdaOutputStream(OutputStream inner) {
      this.inner = inner;
   }

   public void write(int b) throws IOException {
      this.write(new byte[]{(byte)b});
   }

   public void write(byte[] bytes) throws IOException {
      this.write(bytes, 0, bytes.length);
   }

   public void write(byte[] bytes, int offset, int length) throws IOException {
      if (LambdaRuntime.needsDebugLogs) {
         LambdaRuntime.streamLogsToSlicer(bytes, offset, length);
      }

      this.inner.write(bytes, offset, length);
   }
}
