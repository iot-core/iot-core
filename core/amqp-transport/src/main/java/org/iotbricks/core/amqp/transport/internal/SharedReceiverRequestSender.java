package org.iotbricks.core.amqp.transport.internal;

import java.util.UUID;

import org.iotbricks.core.amqp.transport.ReplyStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonReceiver;
import io.vertx.proton.ProtonSender;

public class SharedReceiverRequestSender implements RequestSender {

    private static final Logger logger = LoggerFactory.getLogger(SharedReceiverRequestSender.class);

    private String clientReplyAddress;
    private ProtonReceiver receiver;

    private final Correlator correlator = new Correlator();

    @Override
    public Future<?> connected(final ProtonConnection connection) {

        final ProtonReceiver receiver = connection.createReceiver(UUID.randomUUID().toString());
        final Future<ProtonReceiver> result = Future.future();

        receiver.handler((delivery, message) -> this.correlator.handle(message));

        receiver.openHandler(ready -> {

            try {
                if (ready.failed()) {
                    return;
                }

                final String clientReplyAddress = receiver.getSource().getAddress();

                logger.info("Client reply address: {}", clientReplyAddress);

                this.clientReplyAddress = clientReplyAddress;
                this.receiver = receiver;

            } finally {
                result.handle(ready);
            }
        });

        receiver.open();

        return result;
    }

    @Override
    public boolean isReady() {
        return this.receiver != null;
    }

    @Override
    public void sendRequest(final ProtonSender sender, final Request<?> request,
            final ReplyStrategy replyStrategy) {

        final Object messageId = request.getMessage().getMessageId();

        request.getMessage().setReplyTo(this.clientReplyAddress);

        request.whenClosed(() -> this.correlator.remove(messageId));
        this.correlator.put(messageId, message -> replyStrategy.handleResponse(request, message));

        logger.debug("Sending message: {}", request);

        sender.send(request.getMessage(), delivery -> replyStrategy.handleDelivery(request, delivery));
    }

}