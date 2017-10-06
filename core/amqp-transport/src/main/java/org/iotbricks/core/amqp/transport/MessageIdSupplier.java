package org.iotbricks.core.amqp.transport;

import java.util.UUID;

@FunctionalInterface
public interface MessageIdSupplier<T> {

    /**
     * Create a new, unique, message ID
     *
     * @return a new message ID, may return {@code null}
     */
    public T create();

    public static MessageIdSupplier<UUID> randomUUID() {
        return () -> UUID.randomUUID();
    }

    public static MessageIdSupplier<?> empty() {
        return () -> null;
    }
}
