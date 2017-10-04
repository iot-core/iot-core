package iot.core.services.device.registry.serialization.jackson;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import iot.core.services.device.registry.serialization.ByteSerializer;

public class JacksonSerializer implements ByteSerializer {

    public static ByteSerializer json() {
        return json(false);
    }

    public static ByteSerializer json(final boolean pretty) {
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
                this.mapper.writeValue(closeShield(stream), value);
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
            return this.mapper.readValue(closeShield(stream), clazz);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static OutputStream closeShield(final OutputStream stream) {
        return new FilterOutputStream(stream) {
            @Override
            public void close() {
            }
        };
    }

    private static InputStream closeShield(final InputStream stream) {
        return new FilterInputStream(stream) {
            @Override
            public void close() {
            }
        };
    }

}
