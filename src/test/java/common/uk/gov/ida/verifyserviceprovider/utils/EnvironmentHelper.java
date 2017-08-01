package common.uk.gov.ida.verifyserviceprovider.utils;

import java.lang.reflect.Field;
import java.util.Map;

public class EnvironmentHelper {

    private Map<String, String> map;

    public void setEnv(Map<String, String> map) {
        try {
            this.map = map;
            getWritableEnvironmentMap().putAll(this.map);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable", e);
        }
    }

    public void cleanEnv(){
        try {
            Map<String, String> writableEnvironmentMap = getWritableEnvironmentMap();
            for (Map.Entry<String, String> entry: this.map.entrySet()) {
                writableEnvironmentMap.remove(entry.getKey());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to clean environment variable", e);
        }
    }

    private static Map<String, String> getWritableEnvironmentMap() throws NoSuchFieldException, IllegalAccessException {
        Map<String, String> env = System.getenv();
        Class<?> cl = env.getClass();
        Field field = cl.getDeclaredField("m");
        field.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) field.get(env);
        return result;
    }
}
