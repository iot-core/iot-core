package iot.core.service.binding.proton;

import static java.util.Optional.ofNullable;

import java.util.Optional;

import org.apache.qpid.proton.message.Message;

import io.vertx.proton.ProtonDelivery;
import iot.core.service.binding.amqp.AmqpRequestContext;
import iot.core.services.device.registry.serialization.AmqpSerializer;

public class ProtonRequestContext implements AmqpRequestContext {

    private final Message request;
    private final AmqpSerializer serializer;

    public ProtonRequestContext(final AmqpSerializer serializer, final ProtonDelivery delivery, final Message request) {
        this.serializer = serializer;
        this.request = request;
    }

    @Override
    public Optional<String> getVerb() {
        return ofNullable(request.getSubject());
    }

    @Override
    public Optional<String> getReplyToAddress() {
        return ofNullable(request.getReplyTo());
    }

    @Override
    public <T> T decodeRequest(final Class<T> clazz) {
        return serializer.decode(request.getBody(), clazz);
    }

}
