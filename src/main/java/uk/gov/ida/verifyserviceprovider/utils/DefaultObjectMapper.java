package uk.gov.ida.verifyserviceprovider.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class DefaultObjectMapper {
    public static ObjectMapper OBJECT_MAPPER = new ObjectMapper() {{
        registerModule(new Jdk8Module());
        registerModule(new JavaTimeModule());
    }};
}
