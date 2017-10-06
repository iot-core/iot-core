package org.iotbricks.core.amqp.transport.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.qpid.proton.message.Message;

public class Correlator {

    private final Map<Object, Consumer<Message>> map = new HashMap<>();

    public void remove(final Object messageId) {
        this.map.remove(messageId);
    }

    public void put(final Object messageId, final Consumer<Message> messageHandler) {
        this.map.put(messageId, messageHandler);
    }

    public void handle(final Message message) {
        final Consumer<Message> messageHandler = this.map.remove(message.getCorrelationId());
        if (messageHandler != null) {
            messageHandler.accept(message);
        }
    }

}