package iot.core.amqp.transport;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.glutamate.util.concurrent.CloseableCompletableFuture;
import io.glutamate.util.concurrent.CloseableCompletionStage;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonReceiver;
import io.vertx.proton.ProtonSender;
import iot.core.services.device.registry.serialization.AmqpByteSerializer;
import iot.core.services.device.registry.serialization.AmqpSerializer;
import iot.core.services.device.registry.serialization.jackson.JacksonSerializer;
import iot.core.utils.address.AddressProvider;
import iot.core.utils.address.DefaultAddressProvider;
import iot.core.utils.binding.RequestException;
import iot.core.utils.binding.amqp.AmqpErrorConditionTranslator;
import iot.core.utils.binding.amqp.DefaultAmqpErrorConditionTranslator;

public class AmqpTransport implements Transport<Message> {

    private static final Logger logger = LoggerFactory.getLogger(AmqpTransport.class);

    private static class Request<R> extends CloseableCompletableFuture<R> {
        private final String address;
        private final Message message;
        private final ReplyHandler<R, Message> replyHandler;

        public Request(final String address, final Message message, final ReplyHandler<R, Message> replyHandler) {
            this.address = address;
            this.message = message;
            this.replyHandler = replyHandler;
        }

        public String getAddress() {
            return this.address;
        }

        public String getReplyAddress() {
            return this.message.getReplyTo();
        }

        public Message getMessage() {
            return this.message;
        }

        public void fail(final String reason) {
            fail(new RuntimeException(reason));
        }

        public void fail(final Throwable cause) {
            completeExceptionally(cause);
        }

        @Override
        public boolean completeExceptionally(final Throwable ex) {
            try {
                return super.completeExceptionally(ex);
            } finally {
                try {
                    close();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public boolean complete(final R value) {
            try {
                return super.complete(value);
            } finally {
                try {
                    close();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public void complete(final Message replyMessage) {
            logger.debug("Received reply: {}", replyMessage);

            try {
                complete(this.replyHandler.handleReply(replyMessage));
            } catch (final Exception e) {
                fail(e);
            } finally {
                try {
                    close();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public String toString() {
            return String.format("[Request - %s, %s = %s]", this.address, this.message.getSubject(), this.message);
        }
    }

    private class Buffer {

        private Set<Request<?>> requests = new LinkedHashSet<>();
        private final int limit;

        public Buffer(final int limit) {
            this.limit = limit <= 0 ? Integer.MAX_VALUE : limit;
        }

        public void append(final Request<?> request) {

            final String address = request.getAddress();
            final ProtonSender sender = requestSender(address);

            if (sender != null) {
                logger.debug("Sender is available - {} -> {}", address, sender);
                sendRequest(sender, request);
                return;
            }

            logger.debug("Waiting for sender: {}", address);

            if (this.requests.size() < this.limit) {
                this.requests.add(request);
            } else {
                request.fail("Local send buffer is full");
            }
        }

        public void remove(final Request<?> request) {
            this.requests.remove(request);
        }

        public Request<?> poll(final String address) {
            final Iterator<Request<?>> i = this.requests.iterator();
            while (i.hasNext()) {
                final Request<?> request = i.next();
                if (address.equals(request.getAddress())) {
                    i.remove();
                    return request;
                }
            }

            return null;
        }

        public void flush(final String address, final Consumer<Request<?>> consumer) {
            if (address == null) {
                flush(consumer);
            }

            // FIXME: this needs to be improved

            final List<Request<?>> result = new ArrayList<>();

            final Iterator<Request<?>> i = this.requests.iterator();
            while (i.hasNext()) {
                final Request<?> request = i.next();
                if (address.equals(request.getAddress())) {
                    result.add(request);
                    i.remove();
                }
            }

            result.forEach(consumer);
        }

        public void flush(final Consumer<Request<?>> consumer) {
            final Set<Request<?>> requests = this.requests;
            this.requests = new LinkedHashSet<>();
            requests.forEach(consumer);
        }

        public Set<String> getAddresses() {
            // FIXME: this needs to be improved
            return this.requests
                    .stream()
                    .map(Request::getAddress)
                    .collect(Collectors.toSet());
        }
    }

    public static class Builder {

        private static final AmqpSerializer DEFAULT_SERIALIZER = AmqpByteSerializer.of(JacksonSerializer.json());
        private static final AmqpErrorConditionTranslator DEFAULT_ERROR_CONDITION_TRANSLATOR = DefaultAmqpErrorConditionTranslator
                .instance();
        private static final AddressProvider DEFAULT_ADDRESS_PROVIDER = DefaultAddressProvider.instance();

        private String hostname = "localhost";
        private int port = 5672;
        private String username;
        private String password;
        private String container;
        private AmqpSerializer serializer = DEFAULT_SERIALIZER;
        private AddressProvider addressProvider = DEFAULT_ADDRESS_PROVIDER;
        private AmqpErrorConditionTranslator errorConditionTranslator = DEFAULT_ERROR_CONDITION_TRANSLATOR;
        private int requestBufferSize = -1;

        private Builder() {
        }

        public Builder(final Builder other) {
            this.hostname = other.hostname;
            this.port = other.port;
            this.username = other.username;
            this.password = other.password;
            this.container = other.container;
            this.serializer = other.serializer;
            this.addressProvider = other.addressProvider;
            this.errorConditionTranslator = other.errorConditionTranslator;
        }

        public Builder hostname(final String hostname) {
            this.hostname = hostname;
            return this;
        }

        public String hostname() {
            return this.hostname;
        }

        public Builder port(final int port) {
            this.port = port;
            return this;
        }

        public int port() {
            return this.port;
        }

        public Builder username(final String username) {
            this.username = username;
            return this;
        }

        public String username() {
            return this.username;
        }

        public Builder password(final String password) {
            this.password = password;
            return this;
        }

        public String password() {
            return this.password;
        }

        public Builder container(final String container) {
            this.container = container;
            return this;
        }

        public String container() {
            return this.container;
        }

        public Builder serializer(final AmqpSerializer serializer) {
            this.serializer = serializer != null ? serializer : DEFAULT_SERIALIZER;
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

        public AmqpTransport build(final Vertx vertx) {
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

    private final AtomicBoolean closed = new AtomicBoolean();

    private final Vertx vertx;

    private final Builder options;

    private ProtonConnection connection;

    private final Context context;

    private final Buffer buffer;

    public AmqpTransport(final Vertx vertx, final Builder options) {

        logger.debug("Creating AMQP transport - {}", options);

        this.vertx = vertx;
        this.options = options;

        this.buffer = new Buffer(options.requestBufferSize());

        this.context = vertx.getOrCreateContext();

        this.context.runOnContext(v -> startConnection());
    }

    /**
     * Return if the transport is marked closed.
     *
     * @return {@code true} if the transport is marked closed, {@code false}
     *         otherwise
     */
    public boolean isClosed() {
        return this.closed.get();
    }

    private void startConnection() {
        logger.trace("Starting connection...");

        if (isClosed()) {
            logger.debug("Starting connection... abort, we are closed!");
            // we are marked closed
            return;
        }

        createConnection(this::handleConnection);
    }

    protected void handleConnection(final AsyncResult<ProtonConnection> result) {
        if (result.failed()) {
            if (isClosed()) {
                // we are closed, nothing to do
                return;
            }

            // set up timer for re-connect
            this.vertx.setTimer(1_000, timer -> startConnection());
        } else {
            if (isClosed()) {
                // we got marked closed in the meantime
                result.result().close();
                return;
            }

            this.connection = result.result();
            this.connection.disconnectHandler(this::handleDisconnected);

            this.buffer.getAddresses().forEach(this::requestSender);
        }
    }

    protected void handleDisconnected(final ProtonConnection connection) {

        logger.debug("Got disconnected: {}", connection);

        this.connection = null;
        if (!isClosed()) {
            startConnection();
        }
    }

    @Override
    public void close() throws Exception {
        if (this.closed.compareAndSet(false, true)) {
            this.context.runOnContext(v -> performClose());
        }
    }

    private void performClose() {
        if (this.connection != null) {
            this.connection.close();
            this.connection = null;
        }

        this.buffer.flush(request -> request.fail("Client closed"));
    }

    protected void createConnection(final Handler<AsyncResult<ProtonConnection>> handler) {

        final ProtonClient client = ProtonClient.create(this.vertx);

        client.connect(this.options.hostname(), this.options.port(),
                this.options.username(), this.options.password(),
                con -> {

                    logger.debug("Connection -> {}", con);

                    if (con.failed()) {
                        handler.handle(con);
                        return;
                    }

                    con.result()
                            .setContainer(this.options.container())
                            .openHandler(opened -> {

                                logger.debug("Open -> {}", opened);
                                handler.handle(opened);

                            }).open();

                });
    }

    @Override
    public <R> CloseableCompletionStage<R> request(final String service, final String verb, final Object requestBody,
            final ReplyHandler<R, Message> replyHandler) {

        if (isClosed()) {
            // early fail
            final CloseableCompletableFuture<R> result = new CloseableCompletableFuture<>();
            result.completeExceptionally(new RuntimeException("Client closed"));
            return result;
        }

        final String address = this.options.addressProvider().requestAddress(service);
        final String replyToAddress = this.options.addressProvider().replyAddress(service, createReplyToken());
        final Message message = createMessage(verb, requestBody, replyToAddress);

        final Request<R> request = new Request<>(address, message, replyHandler);

        this.context.runOnContext(v -> {
            startRequest(request);
        });

        return request;
    }

    protected String createReplyToken() {
        return UUID.randomUUID().toString();
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

    private ProtonSender requestSender(final String address) {
        if (this.connection == null) {
            logger.debug("No connection");
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

            final Request<?> request = this.buffer.poll(address);
            logger.debug("Next request: {}", request);

            if (request == null) {
                break;
            }

            sendRequest(sender, request);
        }
    }

    private void sendRequest(final ProtonSender sender, final Request<?> request) {
        final ProtonReceiver receiver = this.connection.createReceiver(request.getReplyAddress());
        receiver.openHandler(ready -> {

            logger.debug("Receiver -> {}", ready);

            if (ready.failed()) {
                request.fail(ready.cause());
                return;
            }

            request.whenClosed(() -> receiver.close());
            ready.result().handler((delivery, message) -> request.complete(message));

            logger.debug("Sending message: {}", request);

            sender.send(request.getMessage(), delivery -> {
                final DeliveryState state = delivery.getRemoteState();

                logger.debug("Remote state - {} for {}", state, request);

                if (state instanceof Rejected) {
                    request.fail(unwrapRemoteException((Rejected) state));
                }

            });
        });
        receiver.open();
    }

    private Exception unwrapRemoteException(final Rejected state) {

        final ErrorCondition error = state.getError();
        if (error == null || error.getCondition() == null) {
            return new RuntimeException("Unknown remote exception");
        }

        final iot.core.utils.binding.ErrorCondition condition = this.options.errorConditionTranslator()
                .fromAmqp(error.getCondition().toString());
        final String message = state.getError().getDescription();

        return new RequestException(condition, message);
    }

    private Message createMessage(final String verb, final Object request, final String replyToAddress) {
        final Properties p = new Properties();
        p.setSubject(verb);
        p.setReplyTo(replyToAddress);

        final Message message = Message.Factory.create();

        message.setProperties(p);
        message.setBody(this.options.serializer().encode(request));
        return message;
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