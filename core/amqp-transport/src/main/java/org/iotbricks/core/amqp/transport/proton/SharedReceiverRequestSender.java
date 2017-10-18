package org.iotbricks.core.amqp.transport.proton;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.qpid.proton.amqp.messaging.Terminus;
import org.apache.qpid.proton.amqp.transport.Source;
import org.iotbricks.core.amqp.transport.RequestInstance;
import org.iotbricks.core.amqp.transport.internal.Correlator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonReceiver;
import io.vertx.proton.ProtonSender;

public class SharedReceiverRequestSender<RQ extends Request> implements RequestSender<RQ> {

    private static final Logger logger = LoggerFactory.getLogger(SharedReceiverRequestSender.class);

    private String clientReplyAddress;
    private ProtonReceiver receiver;

    private final Correlator correlator = new Correlator();

    private final Supplier<String> replyAddressProvider;

    private SharedReceiverRequestSender(final Supplier<String> replyAddressProvider) {
        this.replyAddressProvider = replyAddressProvider;
    }

    @Override
    public Future<?> connected(final ProtonConnection connection) {

        final String address = this.replyAddressProvider.get();

        final ProtonReceiver receiver = connection.createReceiver(address);

        final Supplier<String> addressSupplier;

        if (address == null) {
            final Source source = receiver.getSource();
            if (source instanceof Terminus) {
                ((Terminus) source).setDynamic(true);
                addressSupplier = () -> receiver.getRemoteSource().getAddress();
            } else {
                return Future.failedFuture("Use of dynamic address requested, but source is not of type "
                        + Terminus.class.getSimpleName());
            }
        } else {
            addressSupplier = () -> address;
        }

        final Future<ProtonReceiver> result = Future.future();

        receiver.handler((delivery, message) -> this.correlator.handle(message));

        receiver.openHandler(ready -> {

            try {
                if (ready.failed()) {
                    return;
                }

                final String clientReplyAddress = addressSupplier.get();

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
    public void sendRequest(final ProtonSender sender, final RequestInstance<?, RQ> request) {

        final Object messageId = request.getMessage().getMessageId();

        request.getMessage().setReplyTo(this.clientReplyAddress);

        request.whenClosed(() -> this.correlator.remove(messageId));
        this.correlator.put(messageId, message -> request.handleResponse(message));

        logger.debug("Sending message: {}", request);

        sender.send(request.getMessage(), delivery -> request.handleDelivery(delivery));
    }

    /**
     * Create a new request sender using a client shared dynamic address.
     *
     * @return a new request sender
     */
    public static <RQ extends Request> RequestSender<RQ> dynamic() {
        return new SharedReceiverRequestSender<>(() -> null);
    }

    /**
     * Create a new request sender using a client shared address.
     *
     * @param replyAddressProvider
     *            The address provider for the client reply address
     *
     * @return a new request sender
     */
    public static <RQ extends Request> RequestSender<RQ> of(final Supplier<String> replyAddressProvider) {
        Objects.requireNonNull(replyAddressProvider);
        return new SharedReceiverRequestSender<>(replyAddressProvider);
    }

    /**
     * Create a new request sender using a client shared address.
     *
     * @param replyAddressProvider
     *            The address provider for the client reply address
     *
     * @return a new request sender
     */
    public static <RQ extends Request> RequestSender<RQ> id(final Supplier<String> tokenProvider,
            final Function<String, String> formatter) {

        Objects.requireNonNull(tokenProvider);
        Objects.requireNonNull(formatter);

        return new SharedReceiverRequestSender<>(() -> formatter.apply(tokenProvider.get()));
    }

    /**
     * Create a new request sender using a client shared random UUID address.
     *
     * @return a new request sender
     */
    public static <RQ extends Request> RequestSender<RQ> uuid() {
        return new SharedReceiverRequestSender<>(() -> UUID.randomUUID().toString());
    }

    public static <RQ extends Request> RequestSender<RQ> uuid(final Function<String, String> formatter) {
        return id(() -> UUID.randomUUID().toString(), formatter);
    }

}