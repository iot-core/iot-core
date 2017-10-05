package org.iotbricks.core.binding.amqp;

import org.iotbricks.core.binding.ResponseContext;

public interface AmqpResponseContext<M> extends ResponseContext<M> {
    public void reject(String condition, String description);
}
