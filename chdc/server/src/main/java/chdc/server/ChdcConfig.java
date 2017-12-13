package chdc.server;

/**
 * Defines configuration properties provided by the environment
 */
public class ChdcConfig {

  public static boolean isProduction() {
    return "production".equals(getAppSetting("ENVIRONMENT"));
  }

  public static String getAppSetting(String key) {
    return System.getenv("APPSETTING_" + key);
  }
}
