package org.iotbricks.core.amqp.transport.proton;

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

        private Supplier<RequestSender<RQ>> requestSenderFactory = SharedReceiverRequestSender::dynamic;

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
                    : SharedReceiverRequestSender::dynamic;
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

    private final Buffer<RQ> buffer;
    private final RequestSender<RQ> requestSender;

    protected AbstractAmqpTransport(final Vertx vertx, final Function<RQ, String> addressProvider,
            final Builder<RQ, ? extends AbstractAmqpTransport<?>, ?> options) {
        super(vertx, options);

        this.options = options;

        this.requestSender = options.requestSenderFactory().get();

        this.buffer = new Buffer<>(options.requestBufferSize(), addressProvider, new AmqpTransportContext<RQ>() {

            @Override
            public void sendRequest(final ProtonSender sender, final RequestInstance<?, RQ> request) {
                AbstractAmqpTransport.this.sendRequest(sender, request);
            }

            @Override
            public ProtonSender requestSender(final String service) {
                return AbstractAmqpTransport.this.requestSender(service);
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

            this.buffer.getAddresses().forEach(this::requestSender);
        });
    }

    @Override
    protected void performClose() {
        super.performClose();

        this.buffer.flush(request -> request.fail("Client closed"));
    }

    protected <R> CloseableCompletionStage<R> request(final RQ request, final ReplyStrategy<R, RQ> replyStrategy) {

        if (isClosed()) {
            // early fail
            return CloseableCompletableFuture.failed(new RuntimeException("Client closed"));
        }

        final Message message = request.getMessage();
        message.setMessageId(this.options.messageIdSupplier().create());

        final RequestInstance<R, RQ> requestnstance = new RequestInstance<>(request, replyStrategy);

        this.context.runOnContext(v -> {
            startRequest(requestnstance);
        });

        return requestnstance;
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

    private ProtonSender requestSender(final String address) {
        if (this.connection == null) {
            logger.debug("No connection");
            return null;
        }

        if (!this.requestSender.isReady()) {
            logger.debug("RequestInstance sender is not ready");
            return null;
        }

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
                        logger.debug("Sender queue can accept");
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

            if (logger.isDebugEnabled()) {
                logger.debug("Queue size: {}", sender.getQueued());
            }

            final RequestInstance<?, RQ> request = this.buffer.poll(address);
            logger.debug("Next request: {}", request);

            if (request == null) {
                break;
            }

            sendRequest(sender, request);
        }
    }

    private void sendRequest(final ProtonSender sender, final RequestInstance<?, RQ> request) {
        this.requestSender.sendRequest(sender, request);
    }

}