package org.iotbricks.core.amqp.transport;

import static org.iotbricks.core.amqp.transport.internal.Properties.status;
import static org.iotbricks.core.utils.binding.ErrorCondition.INTERNAL_ERROR;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.amqp.transport.internal.Request;
import org.iotbricks.core.proton.vertx.serializer.AmqpSerializer;
import org.iotbricks.core.utils.binding.ErrorResult;
import org.iotbricks.core.utils.binding.RequestException;
import org.iotbricks.core.utils.binding.amqp.AmqpErrorConditionTranslator;
import org.slf4j.Logger;

import io.vertx.proton.ProtonDelivery;

public class DefaultReplyStrategy implements ReplyStrategy {

    private static final Logger logger = getLogger(DefaultReplyStrategy.class);
    private final AmqpSerializer serializer;
    private final AmqpErrorConditionTranslator errorTranslator;

    public DefaultReplyStrategy(final AmqpSerializer serializer, final AmqpErrorConditionTranslator errorTranslator) {
        this.serializer = serializer;
        this.errorTranslator = errorTranslator;
    }

    @Override
    public void handleDelivery(final Request<?> request, final ProtonDelivery delivery) {
        final DeliveryState state = delivery.getRemoteState();

        logger.debug("Remote state - {} for {}", state, request);

        if (state instanceof Rejected) {
            request.fail(unwrapRemoteExceptionFromReject((Rejected) state));
        }
    }

    @Override
    public void handleResponse(final Request<?> request, final Message message) {

        final Optional<Integer> status = status(message);

        if (status.isPresent() && status.get().equals(500)) {
            // ERROR
            request.fail(unwrapRemoteExceptionFromMessage(message));
        } else {
            request.complete(message);
        }
    }

    private Exception unwrapRemoteExceptionFromMessage(final Message message) {

        final ErrorResult error = this.serializer.decode(message.getBody(), ErrorResult.class);

        org.iotbricks.core.utils.binding.ErrorCondition condition = error.getCondition();

        if (condition == null) {
            condition = INTERNAL_ERROR;
        }

        return new RequestException(condition, error.getMessage());
    }

    private Exception unwrapRemoteExceptionFromReject(final Rejected state) {

        final ErrorCondition error = state.getError();

        if (error == null || error.getCondition() == null) {
            return new RuntimeException("Unknown remote exception");
        }

        final org.iotbricks.core.utils.binding.ErrorCondition condition = this.errorTranslator
                .fromAmqp(error.getCondition().toString());
        final String message = state.getError().getDescription();

        return new RequestException(condition, message);
    }

}
