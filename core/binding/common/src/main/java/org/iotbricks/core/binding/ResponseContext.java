package org.iotbricks.core.binding;

import java.util.function.Consumer;

public interface ResponseContext<M> {
    public default void sendMessage(final String address, final Object value) {
        sendMessage(address, value, message -> {
        });
    }

    public void sendMessage(String address, Object value, Consumer<M> messageCustomizers);
}
