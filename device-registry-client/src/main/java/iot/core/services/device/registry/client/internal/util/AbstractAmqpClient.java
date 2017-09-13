package iot.core.services.device.registry.client.internal.util;

import static java.util.Optional.ofNullable;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonReceiver;
import io.vertx.proton.ProtonSender;
import iot.core.services.device.registry.client.internal.AbstractDefaultClient;
import iot.core.services.device.registry.serialization.Serializer;

public abstract class AbstractAmqpClient extends AbstractDefaultClient {

    private static final Logger logger = LoggerFactory.getLogger(AbstractAmqpClient.class);

    @FunctionalInterface
    public interface ReplyHandler<R> {
        public R handleReply(Message response) throws Exception;
    }

    private final Vertx vertx;

    private final String hostname;

    private final int port;

    private final String container;

    protected Serializer serializer;

    public AbstractAmqpClient(final Vertx vertx, final String hostname, final int port, final String container,
            final Serializer serializer, final long timeoutMs) {
        super(timeoutMs, TimeUnit.MILLISECONDS);

        this.vertx = vertx;
        this.hostname = hostname;
        this.port = port;
        this.container = container;
        this.serializer = serializer;
    }

    protected void withConnection(final Handler<AsyncResult<ProtonConnection>> handler) {
        final ProtonClient client = ProtonClient.create(this.vertx);

        client.connect(this.hostname, this.port, con -> {

            logger.debug("Completed - connect: {}", con);

            if (con.failed()) {
                handler.handle(Future.failedFuture(con.cause()));
                return;
            }

            con.result().setContainer(this.container).openHandler(h -> {

                logger.debug("Container - connect: {}", h);

                if (h.failed()) {
                    con.result().close();
                    handler.handle(Future.failedFuture(con.cause()));
                    return;
                }

                handler.handle(Future.succeededFuture(con.result()));
            }).open();
        });
    }

    protected <R> CompletionStage<R> request(final String address, final String verb, final ByteBuffer body,
            final ReplyHandler<R> replyHandler) {

        // setup message

        final String replyTo = UUID.randomUUID().toString();

        final Properties p = new Properties();
        p.setSubject(verb);
        p.setReplyTo(replyTo);

        final Message message = Message.Factory.create();

        message.setProperties(p);
        message.setBody(new Data(Binary.create(body)));

        // start call

        final CompletableFuture<R> result = new CompletableFuture<>();

        withConnection(con -> {

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

            final ProtonReceiver receiver = con.result().createReceiver(createReplyAddress(address, replyTo));
            receiver.handler((del, msg) -> {
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
                    return;
                }

                // send request

                final ProtonSender sender = con.result().createSender(address);

                sender.openHandler(senderReady -> {

                    logger.debug("senderReady -> {}", senderReady);

                    if (senderReady.failed()) {
                        result.completeExceptionally(senderReady.cause());
                        con.result().close();
                        this.vertx.cancelTimer(timer);
                        return;
                    }

                    sender.send(message);
                }).open();

            });

            receiver.open();

        });

        return result;
    }

    private String createReplyAddress(final String address, final String replyTo) {
        return String.format("%s.reply.%s", address, replyTo);
    }

    protected AbstractAmqpClient.ReplyHandler<Void> ignoreBody() {
        return msg -> null;
    }

    protected <T> AbstractAmqpClient.ReplyHandler<T> bodyAs(final Class<T> clazz) {
        return msg -> this.serializer.decode(Messages.bodyAsBlob(msg), clazz);
    }

    protected <T> AbstractAmqpClient.ReplyHandler<Optional<T>> bodyAsOptional(final Class<T> clazz) {
        return msg -> ofNullable(this.serializer.decode(Messages.bodyAsBlob(msg), clazz));
    }

}