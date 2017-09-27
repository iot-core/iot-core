package iot.core.service.binding.amqp;

import iot.core.service.binding.ResponseContext;

public interface AmqpResponseContext extends ResponseContext {
    public void reject(String condition, String description);
}
