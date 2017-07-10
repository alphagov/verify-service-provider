package common.uk.gov.ida.verifyserviceprovider.utils;

import java.lang.reflect.Field;
import java.util.Map;

public class SystemUtils {

    public static void setEnv(Map<String, String> map) {
        try {
            getWritableEnvironmentMap().putAll(map);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable", e);
        }
    }

    private static Map<String, String> getWritableEnvironmentMap() throws NoSuchFieldException, IllegalAccessException {
        Map<String, String> env = System.getenv();
        Class<?> cl = env.getClass();
        Field field = cl.getDeclaredField("m");
        field.setAccessible(true);
        return (Map<String, String>) field.get(env);
    }


}
