package iot.core.service.binding.amqp;

import java.util.Optional;

import iot.core.service.binding.RequestContext;

public interface AmqpRequestContext extends RequestContext {
    public Optional<String> getReplyToAddress();
}
