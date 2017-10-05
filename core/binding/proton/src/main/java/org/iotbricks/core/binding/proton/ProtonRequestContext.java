package org.iotbricks.core.binding.proton;

import static java.util.Optional.ofNullable;

import java.util.Optional;

import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.binding.amqp.AmqpRequestContext;
import org.iotbricks.core.proton.vertx.serializer.AmqpSerializer;

import io.vertx.proton.ProtonDelivery;

public class ProtonRequestContext implements AmqpRequestContext {

    private final Message request;
    private final AmqpSerializer serializer;

    public ProtonRequestContext(final AmqpSerializer serializer, final ProtonDelivery delivery, final Message request) {
        this.serializer = serializer;
        this.request = request;
    }

    @Override
    public Optional<String> getVerb() {
        return ofNullable(this.request.getSubject());
    }

    @Override
    public Optional<String> getReplyToAddress() {
        return ofNullable(this.request.getReplyTo());
    }

    @Override
    public Object[] decodeRequest(final Class<?>[] parameterTypes) {
        return this.serializer.decode(this.request.getBody(), parameterTypes);
    }

}
