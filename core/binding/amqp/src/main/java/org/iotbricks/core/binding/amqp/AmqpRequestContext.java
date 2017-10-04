package org.iotbricks.core.binding.amqp;

import java.util.Optional;

import org.iotbricks.core.binding.RequestContext;

public interface AmqpRequestContext extends RequestContext {
    public Optional<String> getReplyToAddress();
}
