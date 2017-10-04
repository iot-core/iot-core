package org.iotbricks.core.utils.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public interface ByteSerializer {
    public default byte[] encode(final Object value) {
        if (value == null) {
            return null;
        }

        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            encodeTo(value, stream);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return stream.toByteArray();
    }

    public default ByteBuffer encode(final Object value, final ByteBuffer buffer) {

        // FIXME: fix double allocation

        if (value == null) {
            return buffer;
        }

        final byte[] data = encode(value);
        if (buffer == null) {
            return ByteBuffer.wrap(data);
        } else if (buffer.remaining() >= data.length) {
            buffer.put(data);
            return buffer;
        } else {
            buffer.flip();

            final ByteBuffer newBuffer = ByteBuffer.allocate(buffer.remaining() + data.length);
            newBuffer.put(buffer);
            newBuffer.put(data);
            return newBuffer;
        }
    }

    public void encodeTo(Object value, OutputStream stream) throws IOException;

    public default <T> T decode(final byte[] data, final Class<T> clazz) {
        try {
            return decodeFrom(data != null ? new ByteArrayInputStream(data) : null, clazz);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public default <T> T decode(final ByteBuffer data, final Class<T> clazz) {

        if (data.hasArray()) {
            return decode(data.array(), clazz);
        }

        // FIXME: fix double allocation

        final byte[] buffer = new byte[data.remaining()];
        data.get(buffer);
        return decode(buffer, clazz);
    }

    public <T> T decodeFrom(InputStream stream, Class<T> clazz) throws IOException;
}
