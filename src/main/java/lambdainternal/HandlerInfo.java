package lambdainternal;

public final class HandlerInfo {
   public final Class<?> clazz;
   public final String methodName;

   public HandlerInfo(Class<?> clazz, String methodName) {
      this.clazz = clazz;
      this.methodName = methodName;
   }

   public static HandlerInfo fromString(String handler, ClassLoader cl) throws ClassNotFoundException, NoClassDefFoundError, HandlerInfo.InvalidHandlerException {
      int colonLoc = handler.lastIndexOf("::");
      String className;
      String methodName;
      if (colonLoc < 0) {
         className = handler;
         methodName = null;
      } else {
         className = handler.substring(0, colonLoc);
         methodName = handler.substring(colonLoc + 2);
      }

      if (!className.isEmpty() && (methodName == null || !methodName.isEmpty())) {
         return new HandlerInfo(Class.forName(className, true, cl), methodName);
      } else {
         throw new HandlerInfo.InvalidHandlerException();
      }
   }

   public static String className(String handler) {
      int colonLoc = handler.lastIndexOf("::");
      return colonLoc < 0 ? handler : handler.substring(0, colonLoc);
   }

   public static class InvalidHandlerException extends RuntimeException {
      public static final long serialVersionUID = -1L;

      public InvalidHandlerException() {
      }
   }
}
