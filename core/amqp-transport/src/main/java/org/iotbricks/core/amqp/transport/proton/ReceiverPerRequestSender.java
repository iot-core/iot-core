package org.iotbricks.core.amqp.transport.proton;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.amqp.transport.RequestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonReceiver;
import io.vertx.proton.ProtonSender;

/**
 * A request sender which uses a unique response address per request.
 *
 * @param <RQ>
 *            The type of the request
 */
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
    public boolean prepareRequest(final RequestInstance<?, RQ> request) {
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

            final Message message = request.getMessage();
            message.setReplyTo(replyAddress);

            if (logger.isTraceEnabled()) {
                logger.trace("Sending message: {} -> {}: {}",
                        sender.getTarget().getAddress(), message.getReplyTo(), request);
            }

            sender.send(message, delivery -> request.handleDelivery(delivery));
        });
        receiver.open();
    }

}