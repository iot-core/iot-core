package org.iotbricks.core.amqp.transport;

import static java.util.Optional.ofNullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.amqp.transport.internal.AmqpTransportContext;
import org.iotbricks.core.amqp.transport.internal.Buffer;
import org.iotbricks.core.amqp.transport.internal.Request;
import org.iotbricks.core.amqp.transport.internal.RequestSender;
import org.iotbricks.core.amqp.transport.internal.SharedReceiverRequestSender;
import org.iotbricks.core.proton.vertx.AbstractProtonConnection;
import org.iotbricks.core.proton.vertx.serializer.AmqpSerializer;
import org.iotbricks.core.utils.address.AddressProvider;
import org.iotbricks.core.utils.address.DefaultAddressProvider;
import org.iotbricks.core.utils.binding.amqp.AmqpErrorConditionTranslator;
import org.iotbricks.core.utils.binding.amqp.DefaultAmqpErrorConditionTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.glutamate.util.concurrent.CloseableCompletableFuture;
import io.glutamate.util.concurrent.CloseableCompletionStage;
import io.vertx.core.Vertx;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonSender;

public class AmqpTransport extends AbstractProtonConnection implements Transport<Message> {

    private static final Logger logger = LoggerFactory.getLogger(AmqpTransport.class);

    public static class Builder extends AbstractProtonConnection.Builder<AmqpTransport, Builder> {

        private static final Supplier<RequestSender> DEFAULT_REQUEST_SENDER_FACTORY = SharedReceiverRequestSender::dynamic;
        private static final AmqpErrorConditionTranslator DEFAULT_ERROR_CONDITION_TRANSLATOR = DefaultAmqpErrorConditionTranslator
                .instance();
        private static final AddressProvider DEFAULT_ADDRESS_PROVIDER = DefaultAddressProvider.instance();
        private static final MessageIdSupplier<?> DEFAULT_MESSAGE_ID_SUPPLIER = MessageIdSupplier.randomUUID();

        private AmqpSerializer serializer;
        private AddressProvider addressProvider = DEFAULT_ADDRESS_PROVIDER;
        private AmqpErrorConditionTranslator errorConditionTranslator = DEFAULT_ERROR_CONDITION_TRANSLATOR;
        private int requestBufferSize = -1;

        private MessageIdSupplier<?> messageIdSupplier = DEFAULT_MESSAGE_ID_SUPPLIER;

        private Supplier<RequestSender> requestSenderFactory = DEFAULT_REQUEST_SENDER_FACTORY;

        private Builder() {
        }

        private Builder(final Builder other) {
            super(other);

            this.serializer = other.serializer;
            this.addressProvider = other.addressProvider;
            this.errorConditionTranslator = other.errorConditionTranslator;
            this.messageIdSupplier = other.messageIdSupplier;
            this.requestSenderFactory = other.requestSenderFactory;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder serializer(final AmqpSerializer serializer) {
            Objects.requireNonNull(serializer);
            this.serializer = serializer;
            return this;
        }

        public AmqpSerializer serializer() {
            return this.serializer;
        }

        public Builder addressProvider(final AddressProvider addressProvider) {
            this.addressProvider = addressProvider != null ? addressProvider : DEFAULT_ADDRESS_PROVIDER;
            return this;
        }

        public AddressProvider addressProvider() {
            return this.addressProvider;
        }

        public Builder errorConditionTranslator(final AmqpErrorConditionTranslator errorConditionTranslator) {
            this.errorConditionTranslator = errorConditionTranslator != null ? errorConditionTranslator
                    : DEFAULT_ERROR_CONDITION_TRANSLATOR;
            return this;
        }

        public AmqpErrorConditionTranslator errorConditionTranslator() {
            return this.errorConditionTranslator;
        }

        public Builder requestBufferSize(final int requestBufferSize) {
            this.requestBufferSize = requestBufferSize;
            return this;
        }

        public int requestBufferSize() {
            return this.requestBufferSize;
        }

        public Builder messageIdSupplier(final MessageIdSupplier<?> messageIdSupplier) {
            this.messageIdSupplier = messageIdSupplier != null ? messageIdSupplier : DEFAULT_MESSAGE_ID_SUPPLIER;
            return this;
        }

        public MessageIdSupplier<?> messageIdSupplier() {
            return this.messageIdSupplier;
        }

        public Builder requestSenderFactory(final Supplier<RequestSender> requestSenderFactory) {
            this.requestSenderFactory = requestSenderFactory != null ? requestSenderFactory
                    : DEFAULT_REQUEST_SENDER_FACTORY;
            return this;
        }

        public Supplier<RequestSender> requestSenderFactory() {
            return this.requestSenderFactory;
        }

        @Override
        public AmqpTransport build(final Vertx vertx) {
            Objects.requireNonNull(this.serializer, "'serializer' must be set");
            return new AmqpTransport(vertx, new Builder(this));
        }
    }

    public static Builder newTransport() {
        return new Builder();
    }

    public static Builder newTransport(final Builder other) {
        Objects.requireNonNull(other);
        return new Builder(other);
    }

    private final Builder options;

    private final Buffer buffer;

    private final ReplyStrategy replyStrategy;

    private final RequestSender requestSender;

    public AmqpTransport(final Vertx vertx, final Builder options) {
        super(vertx, options);

        logger.debug("Creating AMQP transport - {}", options);

        this.options = options;

        this.requestSender = options.requestSenderFactory().get();
        this.requestSender.initialize(new Builder(options));

        this.replyStrategy = new DefaultReplyStrategy(options.serializer(), options.errorConditionTranslator());
        this.buffer = new Buffer(options.requestBufferSize(), new AmqpTransportContext() {

            @Override
            public void sendRequest(final ProtonSender sender, final Request<?> request) {
                AmqpTransport.this.sendRequest(sender, request);
            }

            @Override
            public ProtonSender requestSender(final String service) {
                return AmqpTransport.this.requestSender(service);
            }
        });

        open();
    }

    @Override
    protected void performEstablished(final ProtonConnection connection) {
        super.performEstablished(connection);

        this.requestSender.connected(connection).setHandler(ready -> {
            if (ready.failed()) {
                connection.close();
                return;
            }

            this.buffer.getServices().forEach(this::requestSender);
        });
    }

    @Override
    protected void performClose() {
        super.performClose();

        this.buffer.flush(request -> request.fail("Client closed"));
    }

    protected <R> CloseableCompletionStage<R> request(final String service, final String verb, final Message message,
            final ReplyHandler<R, Message> replyHandler) {

        if (isClosed()) {
            // early fail
            final CloseableCompletableFuture<R> result = new CloseableCompletableFuture<>();
            result.completeExceptionally(new RuntimeException("Client closed"));
            return result;
        }

        message.setSubject(verb);
        message.setMessageId(this.options.messageIdSupplier().create());

        final Request<R> request = new Request<>(service, message, replyHandler);

        this.context.runOnContext(v -> {
            startRequest(request);
        });

        return request;
    }

    @Override
    public <R> CloseableCompletionStage<R> request(final String service, final String verb, final Object[] requestBody,
            final ReplyHandler<R, Message> replyHandler) {

        final Message message = Message.Factory.create();
        message.setBody(this.options.serializer().encode(requestBody));

        return request(service, verb, message, replyHandler);
    }

    @Override
    public <R> CloseableCompletionStage<R> request(final String service, final String verb, final Object requestBody,
            final ReplyHandler<R, Message> replyHandler) {

        final Message message = Message.Factory.create();
        message.setBody(this.options.serializer().encode(requestBody));

        return request(service, verb, message, replyHandler);
    }

    private <R> void startRequest(final Request<R> request) {
        if (isClosed()) {
            logger.debug("Client is closed when processing request");
            request.fail("Client closed");
            return;
        }

        request.whenClosed(() -> this.buffer.remove(request));
        this.buffer.append(request);
    }

    private ProtonSender requestSender(final String service) {
        if (this.connection == null) {
            logger.debug("No connection");
            return null;
        }

        if (!this.requestSender.isReady()) {
            logger.debug("Request sender is not ready");
            return null;
        }

        final String address = this.options.addressProvider().requestAddress(service);

        final ProtonSender sender = this.connection.attachments().get("sender." + service, ProtonSender.class);

        logger.debug("Checking for existing sender -> {}", sender);

        if (sender == null) {
            logger.debug("Creating new sender");

            final ProtonSender newSender = this.connection.createSender(address);
            newSender.openHandler(senderReady -> {
                logger.debug("Sender ready -> {}", senderReady);

                if (senderReady.failed()) {
                    senderFailed(service);
                } else {
                    senderReady.result().sendQueueDrainHandler(v -> {
                        logger.debug("Sender queue can accept");
                        senderReady(senderReady.result(), service);
                    });
                }
            });
            newSender.open();

            this.connection.attachments().set("sender." + service, ProtonSender.class, newSender);

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

    protected void senderFailed(final String service) {
        logger.debug("Sender failed ... flushing requests");
        this.buffer.flush(service, request -> request.fail("Sender failed"));
    }

    protected void senderReady(final ProtonSender sender, final String service) {
        logger.debug("Sender ready for {} - > {}", service, sender);

        while (!sender.sendQueueFull()) {

            if (logger.isDebugEnabled()) {
                logger.debug("Queue size: {}", sender.getQueued());
            }

            final Request<?> request = this.buffer.poll(service);
            logger.debug("Next request: {}", request);

            if (request == null) {
                break;
            }

            sendRequest(sender, request);
        }
    }

    private void sendRequest(final ProtonSender sender, final Request<?> request) {
        this.requestSender.sendRequest(sender, request, this.replyStrategy);
    }

    @Override
    public ReplyHandler<Void, Message> ignoreBody() {
        return msg -> null;
    }

    @Override
    public <T> ReplyHandler<T, Message> bodyAs(final Class<T> clazz) {
        return msg -> this.options.serializer().decode(msg.getBody(), clazz);
    }

    @Override
    public <T> ReplyHandler<Optional<T>, Message> bodyAsOptional(final Class<T> clazz) {
        return msg -> ofNullable(this.options.serializer().decode(msg.getBody(), clazz));
    }

}