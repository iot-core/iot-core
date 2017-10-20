package org.iotbricks.core.amqp.transport.proton;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.amqp.transport.MessageIdSupplier;
import org.iotbricks.core.amqp.transport.RequestInstance;
import org.iotbricks.core.amqp.transport.internal.AmqpTransportContext;
import org.iotbricks.core.amqp.transport.internal.Buffer;
import org.iotbricks.core.proton.vertx.AbstractProtonConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.glutamate.util.concurrent.CloseableCompletableFuture;
import io.glutamate.util.concurrent.CloseableCompletionStage;
import io.vertx.core.Vertx;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonSender;

public abstract class AbstractAmqpTransport<RQ extends Request> extends AbstractProtonConnection {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAmqpTransport.class);

    public static abstract class Builder<RQ extends Request, C extends AbstractAmqpTransport<?>, B extends Builder<RQ, C, B>>
            extends AbstractProtonConnection.Builder<C, B> {

        private static final MessageIdSupplier<?> DEFAULT_MESSAGE_ID_SUPPLIER = MessageIdSupplier.randomUUID();

        private MessageIdSupplier<?> messageIdSupplier = DEFAULT_MESSAGE_ID_SUPPLIER;

        private int requestBufferSize = -1;

        private Supplier<RequestSender<RQ>> requestSenderFactory = SharedClientReceiverRequestSender::dynamic;

        protected Builder() {
        }

        protected Builder(final B other) {
            super(other);

            this.messageIdSupplier = other.messageIdSupplier();
            this.requestBufferSize = other.requestBufferSize();
            this.requestSenderFactory = other.requestSenderFactory();
        }

        public B messageIdSupplier(final MessageIdSupplier<?> messageIdSupplier) {
            this.messageIdSupplier = messageIdSupplier != null ? messageIdSupplier : DEFAULT_MESSAGE_ID_SUPPLIER;
            return builder();
        }

        public MessageIdSupplier<?> messageIdSupplier() {
            return this.messageIdSupplier;
        }

        public B requestBufferSize(final int requestBufferSize) {
            this.requestBufferSize = requestBufferSize;
            return builder();
        }

        public int requestBufferSize() {
            return this.requestBufferSize;
        }

        public B requestSenderFactory(final Supplier<RequestSender<RQ>> requestSenderFactory) {
            this.requestSenderFactory = requestSenderFactory != null ? requestSenderFactory
                    : SharedClientReceiverRequestSender::dynamic;
            return builder();
        }

        public Supplier<RequestSender<RQ>> requestSenderFactory() {
            return this.requestSenderFactory;
        }

        @Override
        public void validate() {
            super.validate();
        }
    }

    private final Builder<RQ, ? extends AbstractAmqpTransport<?>, ?> options;

    private final RequestSender<RQ> requestSender;
    private Function<RQ, String> addressProvider;

    private final Buffer<RQ> buffer;

    protected AbstractAmqpTransport(final Vertx vertx, final Function<RQ, String> addressProvider,
            final Builder<RQ, ? extends AbstractAmqpTransport<?>, ?> options) {
        super(vertx, options);

        this.options = options;

        this.requestSender = options.requestSenderFactory().get();
        this.addressProvider = addressProvider;

        this.buffer = new Buffer<>(options.requestBufferSize(), new AmqpTransportContext<RQ>() {

            @Override
            public void sendRequest(final ProtonSender sender, final RequestInstance<?, RQ> request) {
                AbstractAmqpTransport.this.sendRequest(sender, request);
            }

            @Override
            public ProtonSender requestSender(final RequestInstance<?, RQ> request) {
                return AbstractAmqpTransport.this.requestSender(request);
            }
        });

        this.requestSender.initialize(new RequestSenderContext<RQ>() {

            @Override
            public Buffer<RQ> getBuffer() {
                return AbstractAmqpTransport.this.buffer;
            }

            @Override
            public void senderReady(final String address) {
                AbstractAmqpTransport.this.requestSenderReady(address);
            }
        });
    }

    @Override
    protected void performEstablished(final ProtonConnection connection) {
        super.performEstablished(connection);

        this.requestSender.connected(connection).setHandler(ready -> {
            if (ready.failed()) {
                connection.close();
                return;
            }

            /*
             * For each target address we request the sender for the first time. But only
             * for the first request. Target addresses might be mapped to difference
             * receiver addresses. Which could end up in a N*M combination of addresses.
             * However we want to maintain the order of requests for each target address.
             */

            final Set<String> addresses = new HashSet<>();

            for (final RequestInstance<?, RQ> requestInstance : this.buffer.getRequests()) {
                final String address = this.addressProvider.apply(requestInstance.getRequest());
                if (addresses.add(address)) {
                    requestSender(requestInstance);
                }
            }
        });
    }

    @Override
    protected void performClose() {
        super.performClose();

        this.buffer.flush(request -> request.fail("Client closed"));
    }

    protected <R> CloseableCompletionStage<R> request(final RQ request, final ReplyStrategy<R, RQ> replyStrategy) {

        if (request == null || replyStrategy == null) {
            return CloseableCompletableFuture.failed(new NullPointerException());
        }

        if (isClosed()) {
            // early fail
            return CloseableCompletableFuture.failed(new RuntimeException("Client closed"));
        }

        final Message message = request.getMessage();
        if (message == null) {
            return CloseableCompletableFuture.failed(new NullPointerException("Request has a null message"));
        }
        message.setMessageId(this.options.messageIdSupplier().create());

        final String address = this.addressProvider.apply(request);
        if (address == null || address.isEmpty()) {
            return CloseableCompletableFuture.failed(new RuntimeException("Empty or null target address"));
        }

        final RequestInstance<R, RQ> requestInstance = new RequestInstance<>(address, request, replyStrategy);

        this.context.runOnContext(v -> {
            startRequest(requestInstance);
        });

        return requestInstance;
    }

    private <T> void startRequest(final RequestInstance<T, RQ> request) {
        if (isClosed()) {
            logger.debug("Client is closed when processing request");
            request.fail("Client closed");
            return;
        }

        request.whenClosed(() -> this.buffer.remove(request));
        this.buffer.append(request);
    }

    private ProtonSender requestSender(final RequestInstance<?, RQ> request) {
        if (this.connection == null) {
            logger.debug("No connection");
            return null;
        }

        if (!this.requestSender.prepareRequest(request)) {
            /*
             * The request sender is not ready now. But it will inform us by calling
             * requestSenderReady() once it is good to go.
             */
            logger.debug("Request sender is not ready");
            return null;
        }

        final String address = request.getAddress();

        final ProtonSender sender = this.connection.attachments().get("sender." + address, ProtonSender.class);

        logger.debug("Checking for existing sender -> {}", sender);

        if (sender == null) {
            logger.debug("Creating new sender");

            final ProtonSender newSender = this.connection.createSender(address);
            newSender.openHandler(senderReady -> {
                logger.debug("Sender ready -> {}", senderReady);

                if (senderReady.failed()) {
                    senderFailed(address);
                } else {
                    senderReady.result().sendQueueDrainHandler(v -> {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Sender drained: {}", senderReady.result().getTarget().getAddress());
                        }
                        senderReady(senderReady.result(), address);
                    });
                }
            });
            newSender.open();

            this.connection.attachments().set("sender." + address, ProtonSender.class, newSender);

            // wait until the sender is ready
            return null;
        }

        if (!sender.isOpen()) {
            logger.debug("Sender is present but not open");
            // wait until it is open
            return null;
        }

        if (sender.sendQueueFull()) {
            logger.debug("Sender is present but full");
            // wait until the queue has space
            return null;
        }

        return sender;
    }

    protected void senderFailed(final String address) {
        logger.debug("Sender failed ... flushing requests");
        this.buffer.flush(address, request -> request.fail("Sender failed"));
    }

    protected void senderReady(final ProtonSender sender, final String address) {
        logger.debug("Sender ready for {} - > {}", address, sender);

        while (!sender.sendQueueFull()) {

            if (logger.isTraceEnabled()) {
                logger.trace("Queue size: {}", sender.getQueued());
            }

            final RequestInstance<?, RQ> request = this.buffer.poll(address);
            logger.trace("Next request: {}", request);

            if (request == null) {
                break;
            }

            sendRequest(sender, request);
        }
    }

    private void requestSenderReady(final String address) {
        // the receiver is ready, now we need to wait for the sender

        logger.debug("Request sender notified us it is ready for: {}", address);

        this.buffer.flush(address);

    }

    private void sendRequest(final ProtonSender sender, final RequestInstance<?, RQ> request) {
        this.requestSender.sendRequest(sender, request);
    }

}