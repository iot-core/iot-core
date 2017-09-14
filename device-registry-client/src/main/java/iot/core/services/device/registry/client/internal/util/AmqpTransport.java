package iot.core.services.device.registry.client.internal.util;

import static java.util.Optional.ofNullable;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonReceiver;
import io.vertx.proton.ProtonSender;
import iot.core.services.device.registry.serialization.Serializer;
import iot.core.utils.address.AddressProvider;

public class AmqpTransport implements Transport<Message> {

    private static final Logger logger = LoggerFactory.getLogger(AmqpTransport.class);

    private final Vertx vertx;

    private final String hostname;

    private final int port;

    private final String container;

    private final Serializer serializer;

    private final AddressProvider addressProvider;

    public AmqpTransport(final Vertx vertx, final String hostname, final int port, final String container,
            final Serializer serializer, final AddressProvider addressProvider) {

        this.vertx = vertx;
        this.hostname = hostname;
        this.port = port;
        this.container = container;
        this.serializer = serializer;
        this.addressProvider = addressProvider;
    }

    protected Future<ProtonConnection> createConnection() {

        final Future<ProtonConnection> result = Future.future();

        final ProtonClient client = ProtonClient.create(this.vertx);

        client.connect(this.hostname, this.port, con -> {

            logger.debug("Connection -> {}", con);

            if (con.failed()) {
                result.fail(con.cause());
                return;
            }

            con.result()
                    .setContainer(this.container)
                    .openHandler(opened -> {

                        logger.debug("Open - {}", opened);

                        if (opened.failed()) {
                            result.fail(opened.cause());
                            return;
                        }

                        result.complete(opened.result());
                    }).open();

        });

        return result;
    }

    @Override
    public <R> CompletionStage<R> request(final String service, final String verb, final Object request,
            final ReplyHandler<R, Message> replyHandler) {

        // setup message

        final String replyTo = UUID.randomUUID().toString();
        final String replyToAddress = this.addressProvider.replyAddress(service, replyTo);

        final Properties p = new Properties();
        p.setSubject(verb);
        p.setReplyTo(replyToAddress);

        final Message message = Message.Factory.create();

        message.setProperties(p);
        message.setBody(new Data(new Binary(this.serializer.encode(request))));

        // start call

        final CompletableFuture<R> result = new CompletableFuture<>();

        createConnection().setHandler(con -> {

            logger.debug("With connection - {}", con);

            if (con.failed()) {
                return;
            }

            final long timer = this.vertx.setPeriodic(1000, t -> {
                logger.debug("Checking cancellation");
                if (result.isCancelled()) {
                    logger.debug("Closing connection by cancellation");
                    con.result().close();
                }
                if (result.isDone()) {
                    this.vertx.cancelTimer(t);
                }
            });

            // setup receiver

            final ProtonReceiver receiver = con.result()
                    .createReceiver(replyToAddress);
            receiver.handler((del, msg) -> {

                logger.debug("Received result - {}", msg);

                try {
                    result.complete(replyHandler.handleReply(msg));
                } catch (final Exception e) {
                    result.completeExceptionally(e);
                } finally {
                    con.result().close();
                    this.vertx.cancelTimer(timer);
                }
            });

            receiver.openHandler(receiverReady -> {

                if (receiverReady.failed()) {
                    con.result().close();
                    this.vertx.cancelTimer(timer);
                    result.completeExceptionally(receiverReady.cause());
                    return;
                }

                // send request

                final ProtonSender sender = con.result().createSender(service);

                sender.openHandler(senderReady -> {

                    logger.debug("senderReady -> {}", senderReady);

                    if (senderReady.failed()) {
                        con.result().close();
                        this.vertx.cancelTimer(timer);
                        result.completeExceptionally(senderReady.cause());
                        return;
                    }

                    logger.debug("Sending request");

                    sender.send(message);
                }).open();

            });

            receiver.open();

        });

        return result;
    }

    @Override
    public ReplyHandler<Void, Message> ignoreBody() {
        return msg -> null;
    }

    @Override
    public <T> ReplyHandler<T, Message> bodyAs(final Class<T> clazz) {
        return msg -> this.serializer.decode(Messages.bodyAsBlob(msg), clazz);
    }

    @Override
    public <T> ReplyHandler<Optional<T>, Message> bodyAsOptional(final Class<T> clazz) {
        return msg -> ofNullable(this.serializer.decode(Messages.bodyAsBlob(msg), clazz));
    }

}