package org.iotbricks.core.serialization.jackson;

import static io.glutamate.io.Close.shield;
import static io.glutamate.lang.Exceptions.wrap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import org.iotbricks.core.utils.serializer.AbstractByteSerializer;
import org.iotbricks.core.utils.serializer.StringSerializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonSerializer extends AbstractByteSerializer implements StringSerializer {

    public static JacksonSerializer json() {
        return json(false);
    }

    public static JacksonSerializer json(final boolean pretty) {
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
    public <T> T decodeFrom(final InputStream stream, final Class<T> clazz) throws IOException {
        return decodeFrom(this.mapper.getFactory().createParser(shield(stream)), clazz);
    }

    @Override
    public Object[] decodeFrom(final InputStream stream, final Class<?>[] clazzes) throws IOException {
        return decodeFrom(this.mapper.getFactory().createParser(shield(stream)), clazzes);
    }

    protected <T> T decodeFrom(final JsonParser parser, final Class<T> clazz) throws IOException {
        Objects.requireNonNull(clazz);
        return parser.readValueAs(clazz);
    }

    protected Object[] decodeFrom(final JsonParser parser, final Class<?>[] clazzes) throws IOException {

        Objects.requireNonNull(clazzes);

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
    }

    @Override
    public <T> T decodeString(final String data, final Class<T> clazz) {
        return wrap(() -> decodeFrom(this.mapper.getFactory().createParser(data), clazz));
    }

    @Override
    public Object[] decodeString(final String data, final Class<?>[] clazzes) {
        return wrap(() -> decodeFrom(this.mapper.getFactory().createParser(data), clazzes));
    }

    @Override
    public String encodeString(final Object value) {
        return wrap(() -> this.mapper.writeValueAsString(value));
    }

    @Override
    public String encodeString(final Object[] values) {
        return encodeString((Object) values);
    }

}
