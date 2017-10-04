package iot.core.services.device.registry.serialization.jackson;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class ObjectMappers {

    private ObjectMappers() {
    }

    public static final ObjectMapper defaultJson(final boolean pretty) {
        final ObjectMapper mapper = new ObjectMapper();

        if (pretty) {
            mapper.setDefaultPrettyPrinter(new DefaultPrettyPrinter());
        }

        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());

        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return mapper;
    }
}
