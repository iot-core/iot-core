package org.iotbricks.core.binding.proton;

import java.util.Objects;

import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.binding.RequestContext;
import org.iotbricks.core.binding.RequestHandler;
import org.iotbricks.core.binding.ResponseHandler;
import org.iotbricks.core.binding.common.AbstractRequestProcessor;
import org.iotbricks.core.proton.vertx.serializer.AmqpSerializer;
import org.iotbricks.core.utils.binding.ErrorResult;
import org.iotbricks.core.utils.binding.ErrorTranslator;

import io.vertx.proton.ProtonDelivery;
import io.vertx.proton.ProtonMessageHandler;
import io.vertx.proton.ProtonSender;

public class ProtonRequestProcessor
        extends AbstractRequestProcessor<ProtonRequestContext, ProtonResponseContext, Message> {

    private final AmqpSerializer serializer;

    private final ProtonSender sender;

    public ProtonRequestProcessor(final AmqpSerializer serializer,
            final ProtonSender anonymousSender,
            final ResponseHandler<? super Object, ? super ProtonRequestContext, ? super ProtonResponseContext, Message> success,
            final ResponseHandler<? super ErrorResult, ? super ProtonRequestContext, ? super ProtonResponseContext, Message> error,
            final ErrorTranslator errorTranslator,
            final RequestHandler<RequestContext> handler) {

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
                createResponseContext(delivery, message));
    }

    protected ProtonRequestContext createRequestContext(final ProtonDelivery delivery, final Message message) {
        return new ProtonRequestContext(this.serializer, delivery, message);
    }

    protected ProtonResponseContext createResponseContext(final ProtonDelivery delivery, final Message requestMessage) {
        return new ProtonResponseContext(this.serializer, delivery, this.sender, requestMessage);
    }

}
