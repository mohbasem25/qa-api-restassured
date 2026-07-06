package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Centralised configuration loader for the API test framework.
 * <p>
 * Values are sourced from {@code config.properties} on the classpath and can
 * be overridden at runtime via JVM system properties, e.g.:
 * {@code mvn test -Dbase.uri=https://dummyjson.com}
 * <p>
 * This indirection keeps environment/secret concerns out of test code and
 * makes the suite portable across local, CI, and other environments.
 */
public final class ApiConfig {

    private static final Properties PROPERTIES = new Properties();
    private static final String CONFIG_FILE = "config.properties";

    static {
        try (InputStream input = ApiConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IllegalStateException("Unable to find " + CONFIG_FILE + " on the classpath");
            }
            PROPERTIES.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + CONFIG_FILE, e);
        }
    }

    private ApiConfig() {
        // static utility class
    }

    private static String get(String key) {
        return System.getProperty(key, PROPERTIES.getProperty(key));
    }

    public static String baseUri() {
        return get("base.uri");
    }

    public static String basePath() {
        return get("base.path");
    }

    public static int connectionTimeoutMs() {
        return Integer.parseInt(get("connection.timeout.ms"));
    }

    public static int socketTimeoutMs() {
        return Integer.parseInt(get("socket.timeout.ms"));
    }

    public static long responseTimeSlaMs() {
        return Long.parseLong(get("response.time.sla.ms"));
    }
}
