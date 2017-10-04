package org.iotbricks.core.binding.amqp;

import org.iotbricks.core.binding.ResponseContext;

public interface AmqpResponseContext extends ResponseContext {
    public void reject(String condition, String description);
}
