package iot.core.services.device.registry.serialization.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import iot.core.services.device.registry.serialization.Serializer;

public class JacksonSerializer implements Serializer {

    public static Serializer json() {
        return json(false);
    }

    public static Serializer json(final boolean pretty) {
        return new JacksonSerializer(ObjectMappers.defaultJson(pretty));
    }

    private final ObjectMapper mapper;

    private JacksonSerializer(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void encodeTo(final Object value, final OutputStream stream) throws IOException {
        Objects.requireNonNull(stream);

        if (value != null) {
            try {
                this.mapper.writeValue(stream, value);
            } catch (final JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public <T> T decodeFrom(final InputStream stream, final Class<T> clazz) {

        if (stream == null) {
            return null;
        }

        Objects.requireNonNull(clazz);

        try {
            return this.mapper.readValue(stream, clazz);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}
