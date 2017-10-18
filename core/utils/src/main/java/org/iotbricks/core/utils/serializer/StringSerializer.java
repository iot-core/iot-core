package org.iotbricks.core.utils.serializer;

public interface StringSerializer extends Serializer {

    // === Decoders ====

    public <T> T decodeString(String data, Class<T> clazz);

    public Object[] decodeString(String data, Class<?>[] clazz);

    // === Encoders ====

    public String encodeString(Object value);

    public String encodeString(Object[] values);

}
