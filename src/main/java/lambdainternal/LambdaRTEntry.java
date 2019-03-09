package lambdainternal;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

public class LambdaRTEntry {
   private static SimpleDateFormat FORMAT;
   private static final Comparator<String> NO_SORT_ORDER = null;
   private static final Comparator<String> LEXICAL_SORT_ORDER = Comparator.comparing(String::toString);

   public LambdaRTEntry() {
   }

   public static String logDate() {
      return logDate(System.currentTimeMillis());
   }

   public static String logDate(long millis) {
      if (FORMAT == null) {
         FORMAT = new SimpleDateFormat("dd MMM yyyy HH:mm:ss,SSS");
      }

      return FORMAT.format(new Date(millis));
   }

   public static String getEnvOrExit(String envVariableName) {
      String value = System.getenv(envVariableName);
      if (value == null) {
         System.err.println("Could not get environment variable " + envVariableName);
         System.exit(-1);
      }

      return value;
   }

   private static URL newURL(File parent, String path) {
      try {
         return new URL("file", (String)null, -1, parent.getPath() + "/" + path);
      } catch (MalformedURLException var3) {
         throw new RuntimeException(var3);
      }
   }

   private static void appendJars(File dir, List<URL> result, Comparator<String> sortOrder) {
      if (dir.isDirectory()) {
         String[] names = dir.list();
         if (sortOrder != null) {
            Arrays.sort(names, sortOrder);
         }

         String[] var4 = names;
         int var5 = names.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            String path = var4[var6];
            if (path.endsWith(".jar")) {
               result.add(newURL(dir, path));
            }
         }

      }
   }

   private static URLClassLoader makeClassLoader(ClassLoader parent, List<URL> urls) {
      URL[] allTheUrls = (URL[])urls.toArray(new URL[urls.size()]);
      return new URLClassLoader(allTheUrls, parent);
   }

   public static URLClassLoader makeRuntimeClassLoader(String runtimePath, ClassLoader parent) {
      List<URL> res = new ArrayList();
      appendJars(new File(runtimePath + "/lib"), res, NO_SORT_ORDER);
      return makeClassLoader(parent, res);
   }

   public static URLClassLoader makeCustomerClassLoader(String taskPath, String optPath, ClassLoader parent) {
      File taskDir = new File(taskPath + "/");
      List<URL> res = new ArrayList();
      res.add(newURL(taskDir, ""));
      appendJars(new File(taskPath + "/lib"), res, LEXICAL_SORT_ORDER);
      appendJars(new File(optPath + "/lib"), res, LEXICAL_SORT_ORDER);
      return makeClassLoader(parent, res);
   }

   public static void main(String[] args) throws Throwable {
      String runtimeDir = getEnvOrExit("LAMBDA_RUNTIME_DIR");
      URLClassLoader internalClassLoader = makeRuntimeClassLoader(runtimeDir, ClassLoader.getSystemClassLoader());
      Class.forName("lambdainternal.AWSLambda", true, internalClassLoader);
   }
}
