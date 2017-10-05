package org.iotbricks.core.utils.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class AbstractByteSerializer implements ByteSerializer {

    @FunctionalInterface
    private interface StreamEncoder {
        public <T> void encode(T value, OutputStream stream) throws IOException;
    }

    private <T> byte[] encode(final T value, final StreamEncoder encoder) {
        if (value == null) {
            return null;
        }

        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            encoder.encode(value, stream);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return stream.toByteArray();
    }

    @Override
    public byte[] encode(final Object value) {
        return encode(value, this::encodeTo);
    }

    @Override
    public byte[] encode(final Object[] value) {
        return encode(value, this::encodeTo);
    }

    private <T> ByteBuffer encode(final T value, final ByteBuffer buffer, final Function<T, byte[]> encoder) {

        // FIXME: fix double allocation

        if (value == null) {
            return buffer;
        }

        final byte[] data = encoder.apply(value);
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

    @Override
    public ByteBuffer encode(final Object value, final ByteBuffer buffer) {
        return encode(value, buffer, this::encode);
    }

    @Override
    public ByteBuffer encode(final Object[] value, final ByteBuffer buffer) {
        return encode(value, buffer, this::encode);
    }

    @Override
    public <T> T decode(final byte[] data, final Class<T> clazz) {
        try {
            return decodeFrom(data != null ? new ByteArrayInputStream(data) : null, clazz);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object[] decode(final byte[] data, final Class<?>[] clazzes) {
        try {
            return decodeFrom(data != null ? new ByteArrayInputStream(data) : null, clazzes);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T, C> T decode(final ByteBuffer data, final C clazz, final BiFunction<byte[], C, T> decoder) {

        if (data.hasArray()) {
            return decoder.apply(data.array(), clazz);
        }

        // FIXME: fix double allocation

        final byte[] buffer = new byte[data.remaining()];
        data.get(buffer);
        return decoder.apply(buffer, clazz);
    }

    @Override
    public <T> T decode(final ByteBuffer data, final Class<T> clazz) {
        return decode(data, clazz, this::decode);
    }

    @Override
    public Object[] decode(final ByteBuffer data, final Class<?>[] clazz) {
        return decode(data, clazz, this::decode);
    }

}
