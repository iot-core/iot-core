package iot.core.service.binding.proton;

import java.util.Objects;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.amqp.transport.serializer.AmqpSerializer;

import io.vertx.proton.ProtonDelivery;
import io.vertx.proton.ProtonSender;
import iot.core.service.binding.amqp.AmqpResponseContext;

public class ProtonResponseContext implements AmqpResponseContext {

    private final AmqpSerializer serializer;
    private final ProtonDelivery delivery;
    private ProtonSender sender;

    public ProtonResponseContext(final AmqpSerializer serializer, final ProtonDelivery delivery,
            final ProtonSender anonymousSender) {

        Objects.requireNonNull(serializer);
        Objects.requireNonNull(delivery);
        Objects.requireNonNull(anonymousSender);

        this.serializer = serializer;
        this.delivery = delivery;

        this.sender = anonymousSender;
    }

    @Override
    public void sendMessage(final String address, final Object value) {
        final Message message = Message.Factory.create();

        message.setBody(serializer.encode(value));
        message.setAddress(address);

        sender.send(message);
    }

    @Override
    public void reject(final String condition, final String description) {
        final Rejected rejected = new Rejected();
        final ErrorCondition error = new ErrorCondition(Symbol.getSymbol(condition), description);

        rejected.setError(error);

        this.delivery.disposition(rejected, true);
    }

}
