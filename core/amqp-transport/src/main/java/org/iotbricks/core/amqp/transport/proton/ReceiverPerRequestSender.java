package org.iotbricks.core.amqp.transport.proton;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.iotbricks.core.amqp.transport.RequestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonReceiver;
import io.vertx.proton.ProtonSender;

public class ReceiverPerRequestSender<RQ extends Request> implements RequestSender<RQ> {

    private static final Logger logger = LoggerFactory.getLogger(ReceiverPerRequestSender.class);

    private ProtonConnection connection;

    private final Function<RQ, String> replyAddressProvider;

    public ReceiverPerRequestSender(final Function<RQ, String> replyAddressProvider) {
        this.replyAddressProvider = replyAddressProvider;
    }

    public ReceiverPerRequestSender(final Supplier<String> replyIdGenerator,
            final BiFunction<RQ, String, String> replyAddressProvider) {
        this.replyAddressProvider = request -> replyAddressProvider.apply(request, replyIdGenerator.get());
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
    public void sendRequest(final ProtonSender sender, final RequestInstance<?, RQ> request) {

        final String replyAddress = this.replyAddressProvider.apply(request.getRequest());

        final ProtonReceiver receiver = this.connection.createReceiver(replyAddress);

        receiver.openHandler(ready -> {

            logger.debug("Receiver -> {}", ready);

            if (ready.failed()) {
                request.fail(ready.cause());
                return;
            }

            request.whenClosed(() -> receiver.close());

            ready.result().handler((delivery, message) -> request.handleResponse(message));

            logger.debug("Sending message: {}", request);

            request.getMessage().setReplyTo(replyAddress);
            sender.send(request.getMessage(), delivery -> request.handleDelivery(delivery));
        });
        receiver.open();
    }

}