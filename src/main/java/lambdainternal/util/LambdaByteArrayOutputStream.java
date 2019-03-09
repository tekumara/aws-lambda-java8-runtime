package lambdainternal.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class LambdaByteArrayOutputStream extends ByteArrayOutputStream {
   public LambdaByteArrayOutputStream() {
   }

   public LambdaByteArrayOutputStream(int size) {
      super(size);
   }

   public byte[] getRawBuf() {
      return super.buf;
   }

   public int getValidByteCount() {
      return super.count;
   }

   public void readAll(InputStream input) throws IOException {
      while(true) {
         int numToRead = Math.max(input.available(), 1024);
         this.ensureSpaceAvailable(numToRead);
         int rc = input.read(this.buf, this.count, numToRead);
         if (rc < 0) {
            return;
         }

         this.count += rc;
      }
   }

   private void ensureSpaceAvailable(int space) {
      if (space > 0) {
         int remaining = this.count - this.buf.length;
         if (remaining < space) {
            int newSize = this.buf.length * 2;
            if (newSize < this.buf.length) {
               newSize = 2147483647;
            }

            byte[] newBuf = new byte[newSize];
            System.arraycopy(this.buf, 0, newBuf, 0, this.count);
            this.buf = newBuf;
         }

      }
   }
}
