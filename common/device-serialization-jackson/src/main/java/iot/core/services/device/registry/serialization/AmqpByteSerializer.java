package iot.core.services.device.registry.serialization;

import java.util.Objects;

import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Section;

public final class AmqpByteSerializer implements AmqpSerializer {

    private final ByteSerializer byteSerializer;

    private AmqpByteSerializer(final ByteSerializer byteSerializer) {
        this.byteSerializer = byteSerializer;
    }

    @Override
    public Section encode(final Object object) {
        if (object == null) {
            return null;
        }
        return new Data(new Binary(this.byteSerializer.encode(object)));
    }

    @Override
    public <T> T decode(final Section section, final Class<T> clazz) {
        return this.byteSerializer.decode(((Data) section).getValue().asByteBuffer(), clazz);
    }

    public static AmqpSerializer of(final ByteSerializer byteSerializer) {
        Objects.requireNonNull(byteSerializer);
        return new AmqpByteSerializer(byteSerializer);
    }

}
