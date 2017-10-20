package org.iotbricks.core.amqp.transport.proton;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.amqp.transport.RequestInstance;
import org.iotbricks.core.amqp.transport.internal.Buffer;
import org.iotbricks.core.amqp.transport.internal.Correlator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonReceiver;
import io.vertx.proton.ProtonSender;

/**
 * An abstract request sender which uses a set of shared response addresses.
 *
 * @param <RQ>
 *            The request type
 */
public abstract class AbstractSharedRequestReceiverRequestSender<RQ extends Request> implements RequestSender<RQ> {

    private static final Logger logger = LoggerFactory.getLogger(SharedRequestReceiverRequestSender.class);

    protected abstract String responseAddress(final RequestInstance<?, RQ> request);

    private final Correlator correlator = new Correlator();

    private Buffer<RQ> buffer;
    private RequestSenderContext<RQ> context;

    private ProtonConnection connection;

    protected AbstractSharedRequestReceiverRequestSender() {
    }

    @Override
    public void initialize(final RequestSenderContext<RQ> context) {
        this.buffer = context.getBuffer();
        this.context = context;
    }

    @Override
    public Future<?> connected(final ProtonConnection connection) {

        this.connection = connection;

        final Set<String> receivers = this.buffer.getRequests().stream()
                .map(this::responseAddress)
                .collect(Collectors.toSet());

        @SuppressWarnings("rawtypes")
        final List<Future> futures = new LinkedList<>();

        for (final String address : receivers) {

            final Future<ProtonReceiver> future = Future.future();

            final ProtonReceiver receiver = createReceiver(connection, address);
            attachReceiver(address, receiver);
            receiver.openHandler(future.completer());
            receiver.open();
        }

        return CompositeFuture.all(futures);
    }

    private ProtonReceiver createReceiver(final ProtonConnection connection, final String address) {
        final ProtonReceiver receiver = connection.createReceiver(address);
        receiver.handler((delivery, message) -> this.correlator.handle(message));

        return receiver;
    }

    @Override
    public boolean prepareRequest(final RequestInstance<?, RQ> request) {

        if (this.connection == null) {
            return false;
        }

        final String responseAddress = responseAddress(request);

        final ProtonReceiver receiver = this.connection.attachments()
                .get("receiver." + responseAddress, ProtonReceiver.class);

        if (receiver == null) {
            final ProtonReceiver r = createReceiver(this.connection, responseAddress);

            attachReceiver(responseAddress, r);

            r.openHandler(opened -> {
                if (opened.failed()) {
                    attachReceiver(responseAddress, null);
                    request.fail(new RuntimeException("Failed to open receiver", opened.cause()));
                } else {
                    this.context.senderReady(request.getAddress());
                }
            });

            r.open();

            return false;
        }

        if (!receiver.isOpen()) {
            return false;
        }

        return true;
    }

    private void attachReceiver(final String responseAddress, final ProtonReceiver r) {
        this.connection.attachments()
                .set("receiver." + responseAddress, ProtonReceiver.class, r);
    }

    @Override
    public void sendRequest(final ProtonSender sender, final RequestInstance<?, RQ> request) {

        final Message message = request.getMessage();
        final Object messageId = message.getMessageId();

        final String address = responseAddress(request);

        request.getMessage().setReplyTo(address);

        request.whenClosed(() -> this.correlator.remove(messageId));
        this.correlator.put(messageId, request::handleResponse);

        if (logger.isTraceEnabled()) {
            logger.trace("Sending message: {} -> {}: {}",
                    sender.getTarget().getAddress(), message.getReplyTo(), request);
        }

        sender.send(message, delivery -> request.handleDelivery(delivery));
    }

}