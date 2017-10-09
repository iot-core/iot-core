package org.iotbricks.core.proton.vertx.serializer;

import java.util.Objects;

import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.iotbricks.core.utils.serializer.ByteSerializer;

/**
 * Use a {@link ByteSerializer} to serialize into an AMQP data section.
 * <p>
 * This serializes will delegate serialization to a {@link ByteSerializer} and
 * use the resulting BLOB to create an AMQP data section.
 */
public final class AmqpByteSerializer implements AmqpSerializer {

    private final ByteSerializer byteSerializer;

    private AmqpByteSerializer(final ByteSerializer byteSerializer) {
        this.byteSerializer = byteSerializer;
    }

    @Override
    public String getContentType() {
        return this.byteSerializer.getContentType();
    }

    @Override
    public Section encode(final Object object) {
        if (object == null) {
            return null;
        }
        return new Data(new Binary(this.byteSerializer.encode(object)));
    }

    @Override
    public Section encode(final Object[] objects) {
        return new Data(new Binary(this.byteSerializer.encode(objects)));
    }

    @Override
    public <T> T decode(final Section section, final Class<T> clazz) {
        return this.byteSerializer.decode(((Data) section).getValue().asByteBuffer(), clazz);
    }

    @Override
    public Object[] decode(final Section section, final Class<?>[] clazzes) {
        return this.byteSerializer.decode(((Data) section).getValue().asByteBuffer(), clazzes);
    }

    public static AmqpSerializer of(final ByteSerializer byteSerializer) {
        Objects.requireNonNull(byteSerializer);
        return new AmqpByteSerializer(byteSerializer);
    }

}
