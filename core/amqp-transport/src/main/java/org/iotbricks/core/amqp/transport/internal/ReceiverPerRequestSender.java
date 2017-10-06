package org.iotbricks.core.amqp.transport.internal;

import java.util.UUID;
import java.util.function.Supplier;

import org.iotbricks.core.amqp.transport.AmqpTransport;
import org.iotbricks.core.amqp.transport.ReplyStrategy;
import org.iotbricks.core.utils.address.AddressProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonReceiver;
import io.vertx.proton.ProtonSender;

public class ReceiverPerRequestSender implements RequestSender {

    private static final Logger logger = LoggerFactory.getLogger(ReceiverPerRequestSender.class);

    private ProtonConnection connection;

    private final Supplier<String> replyIdProvider;

    private AddressProvider addressProvider;

    public ReceiverPerRequestSender(final Supplier<String> replyIdProvider) {
        this.replyIdProvider = replyIdProvider;
    }

    public ReceiverPerRequestSender() {
        this(() -> UUID.randomUUID().toString());
    }

    @Override
    public void initialize(final AmqpTransport.Builder options) {
        this.addressProvider = options.addressProvider();
    }

    @Override
    public Future<?> connected(final ProtonConnection connection) {
        this.connection = connection;
        return Future.succeededFuture();
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void sendRequest(final ProtonSender sender, final Request<?> request,
            final ReplyStrategy replyStrategy) {

        final String replyId = this.replyIdProvider.get();
        final String replyAddress = this.addressProvider.replyAddress(request.getService(), replyId);

        final ProtonReceiver receiver = this.connection.createReceiver(replyAddress);

        receiver.openHandler(ready -> {

            logger.debug("Receiver -> {}", ready);

            if (ready.failed()) {
                request.fail(ready.cause());
                return;
            }

            request.whenClosed(() -> receiver.close());

            ready.result().handler((delivery, message) -> replyStrategy.handleResponse(request, message));

            logger.debug("Sending message: {}", request);

            request.getMessage().setReplyTo(replyAddress);
            sender.send(request.getMessage(), delivery -> replyStrategy.handleDelivery(request, delivery));
        });
        receiver.open();
    }

}