package lambdainternal.util;

public final class Functions {
   private Functions() {
   }

   public interface V2<A1, A2> {
      void call(A1 var1, A2 var2);
   }

   public interface V1<A1> {
      void call(A1 var1);
   }

   public interface V0 {
      void call();
   }

   public interface R9<R, A1, A2, A3, A4, A5, A6, A7, A8, A9> {
      R call(A1 var1, A2 var2, A3 var3, A4 var4, A5 var5, A6 var6, A7 var7, A8 var8, A9 var9);
   }

   public interface R5<R, A1, A2, A3, A4, A5> {
      R call(A1 var1, A2 var2, A3 var3, A4 var4, A5 var5);
   }

   public interface R4<R, A1, A2, A3, A4> {
      R call(A1 var1, A2 var2, A3 var3, A4 var4);
   }

   public interface R3<R, A1, A2, A3> {
      R call(A1 var1, A2 var2, A3 var3);
   }

   public interface R2<R, A1, A2> {
      R call(A1 var1, A2 var2);
   }

   public interface R1<R, A1> {
      R call(A1 var1);
   }

   public interface R0<R> {
      R call();
   }
}
