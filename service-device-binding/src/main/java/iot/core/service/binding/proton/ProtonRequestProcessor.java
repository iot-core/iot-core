package iot.core.service.binding.proton;

import java.util.Objects;

import org.apache.qpid.proton.message.Message;

import io.vertx.proton.ProtonDelivery;
import io.vertx.proton.ProtonMessageHandler;
import io.vertx.proton.ProtonSender;
import iot.core.service.binding.RequestHandler;
import iot.core.service.binding.ResponseHandler;
import iot.core.service.binding.common.AbstractRequestProcessor;
import iot.core.services.device.registry.serialization.AmqpSerializer;
import iot.core.utils.binding.ErrorResult;
import iot.core.utils.binding.ErrorTranslator;

public class ProtonRequestProcessor extends AbstractRequestProcessor<ProtonRequestContext, ProtonResponseContext> {

    private final AmqpSerializer serializer;

    private final ProtonSender sender;

    public ProtonRequestProcessor(final AmqpSerializer serializer,
            final ProtonSender anonymousSender,
            final ResponseHandler<? super Object, ? super ProtonRequestContext, ? super ProtonResponseContext> success,
            final ResponseHandler<? super ErrorResult, ? super ProtonRequestContext, ? super ProtonResponseContext> error,
            final ErrorTranslator errorTranslator,
            final RequestHandler<ProtonRequestContext> handler) {

        super(success, error, errorTranslator, handler);

        Objects.requireNonNull(serializer);
        Objects.requireNonNull(anonymousSender);

        if (anonymousSender.getTarget().getAddress() != null) {
            throw new IllegalArgumentException("Sender must be anonymous");
        }

        this.serializer = serializer;
        this.sender = anonymousSender;
    }

    public ProtonMessageHandler messageHandler() {
        return (delivery, message) -> process(
                createRequestContext(delivery, message),
                createResponseContext(delivery));
    }

    protected ProtonRequestContext createRequestContext(final ProtonDelivery delivery, final Message message) {
        return new ProtonRequestContext(serializer, delivery, message);
    }

    protected ProtonResponseContext createResponseContext(final ProtonDelivery delivery) {
        return new ProtonResponseContext(serializer, delivery, sender);
    }

}
