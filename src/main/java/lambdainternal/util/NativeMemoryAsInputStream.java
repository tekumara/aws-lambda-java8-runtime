package lambdainternal.util;

import java.io.IOException;
import java.io.InputStream;
import sun.misc.Unsafe;

public class NativeMemoryAsInputStream extends InputStream {
   private final long nativeStart;
   private final long nativeEnd;
   private long nativeCur;

   public NativeMemoryAsInputStream(long start, long end) {
      this.nativeStart = start;
      this.nativeCur = start;
      this.nativeEnd = end;
   }

   public final int available() {
      return (int)(this.nativeEnd - this.nativeCur);
   }

   public final int read() throws IOException {
      if (this.nativeCur >= this.nativeEnd) {
         return -1;
      } else {
         byte result = UnsafeUtil.TheUnsafe.getByte(this.nativeCur);
         ++this.nativeCur;
         return result & 255;
      }
   }

   public final int read(byte[] b) {
      return this.uncheckedBoundsRead(b, 0, b.length);
   }

   public final int read(byte[] b, int off, int len) {
      if (b == null) {
         throw new NullPointerException();
      } else if (off >= 0 && len >= 0 && len <= b.length - off) {
         return this.uncheckedBoundsRead(b, off, len);
      } else {
         throw new IndexOutOfBoundsException();
      }
   }

   private final int uncheckedBoundsRead(byte[] b, int off, int len) {
      long numRemaining = this.nativeEnd - this.nativeCur;
      if (numRemaining <= 0L) {
         return -1;
      } else {
         return len == 0 ? 0 : this.uncheckedRead(b, off, len, numRemaining);
      }
   }

   private final int uncheckedRead(byte[] b, int off, int len, long numRemaining) {
      int numToRead = (int)Math.min((long)len, numRemaining);
      UnsafeUtil.TheUnsafe.copyMemory((Object)null, this.nativeCur, b, (long)(Unsafe.ARRAY_BYTE_BASE_OFFSET + off), (long)numToRead);
      this.nativeCur += (long)numToRead;
      return numToRead;
   }
}
