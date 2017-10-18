package org.iotbricks.core.utils.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public interface ByteSerializer extends Serializer {

    // === Decoders ====

    public <T> T decodeFrom(InputStream stream, Class<T> clazz) throws IOException;

    public <T> T decode(ByteBuffer data, Class<T> clazz);

    public <T> T decode(byte[] data, Class<T> clazz);

    public Object[] decodeFrom(InputStream stream, Class<?>[] clazz) throws IOException;

    public Object[] decode(ByteBuffer data, Class<?>[] clazz);

    public Object[] decode(byte[] data, Class<?>[] clazz);

    // === Encoders ====

    public void encodeTo(Object value, OutputStream stream) throws IOException;

    public ByteBuffer encode(Object value, ByteBuffer buffer);

    public byte[] encode(Object value);

    public void encodeTo(Object[] value, OutputStream stream) throws IOException;

    public ByteBuffer encode(Object[] value, ByteBuffer buffer);

    public byte[] encode(Object[] value);
}
