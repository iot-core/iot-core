package org.iotbricks.common.device.registry.serialization.jackson;

import static io.glutamate.io.Close.shield;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import org.iotbricks.core.utils.serializer.AbstractByteSerializer;
import org.iotbricks.core.utils.serializer.ByteSerializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonSerializer extends AbstractByteSerializer {

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
    public String getContentType() {
        return "text/json";
    }

    @Override
    public void encodeTo(final Object value, final OutputStream stream) throws IOException {
        Objects.requireNonNull(stream);

        if (value != null) {
            try {
                this.mapper.writeValue(shield(stream), value);
            } catch (final JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void encodeTo(final Object[] values, final OutputStream stream) throws IOException {
        Objects.requireNonNull(values);
        encodeTo((Object) values, stream);
    }

    @Override
    public <T> T decodeFrom(final InputStream stream, final Class<T> clazz) {

        if (stream == null) {
            return null;
        }

        Objects.requireNonNull(clazz);

        try {
            return this.mapper.readValue(shield(stream), clazz);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object[] decodeFrom(final InputStream stream, final Class<?>[] clazzes) {

        if (stream == null) {
            return null;
        }

        Objects.requireNonNull(clazzes);

        try (final JsonParser parser = this.mapper.getFactory().createParser(shield(stream))) {

            final JsonToken token = parser.nextToken();
            parser.nextToken(); // move to first array element

            if (token != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Content is not encoded in array");
            }

            final Object[] result = new Object[clazzes.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = this.mapper.readValue(parser, clazzes[i]);
            }

            return result;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}
