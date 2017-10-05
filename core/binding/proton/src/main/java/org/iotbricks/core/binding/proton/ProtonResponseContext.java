package org.iotbricks.core.binding.proton;

import java.util.Objects;
import java.util.function.Consumer;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.binding.amqp.AmqpResponseContext;
import org.iotbricks.core.proton.vertx.serializer.AmqpSerializer;

import io.vertx.proton.ProtonDelivery;
import io.vertx.proton.ProtonSender;

public class ProtonResponseContext implements AmqpResponseContext<Message> {

    private final AmqpSerializer serializer;
    private final ProtonDelivery delivery;
    private final ProtonSender sender;
    private final Message requestMessage;

    public ProtonResponseContext(final AmqpSerializer serializer, final ProtonDelivery delivery,
            final ProtonSender anonymousSender, final Message requestMessage) {

        Objects.requireNonNull(serializer);
        Objects.requireNonNull(delivery);
        Objects.requireNonNull(anonymousSender);
        Objects.requireNonNull(requestMessage);

        this.serializer = serializer;
        this.delivery = delivery;

        this.sender = anonymousSender;
        this.requestMessage = requestMessage;
    }

    @Override
    public void sendMessage(final String address, final Object value, final Consumer<Message> messageCustomizer) {
        final Message message = Message.Factory.create();

        message.setBody(this.serializer.encode(value));
        message.setAddress(address);
        message.setCorrelationId(this.requestMessage.getMessageId());

        if (messageCustomizer != null) {
            messageCustomizer.accept(message);
        }

        this.sender.send(message);
    }

    @Override
    public void reject(final String condition, final String description) {
        final Rejected rejected = new Rejected();
        final ErrorCondition error = new ErrorCondition(Symbol.getSymbol(condition), description);

        rejected.setError(error);

        this.delivery.disposition(rejected, true);
    }

}
