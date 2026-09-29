package az.crm.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigProvider {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream in = ConfigProvider.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                PROPERTIES.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load config.properties", e);
        }
    }

    private ConfigProvider() {
    }

    public static String get(String key) {
        String systemOverride = System.getProperty(key);
        if (systemOverride != null && !systemOverride.isBlank()) {
            return systemOverride;
        }
        return PROPERTIES.getProperty(key);
    }

    public static String baseUrl() {
        return get("base.url");
    }

    public static String username() {
        return get("api.username");
    }

    public static String password() {
        return get("api.password");
    }
}
