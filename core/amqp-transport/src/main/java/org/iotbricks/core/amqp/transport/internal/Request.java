package org.iotbricks.core.amqp.transport.internal;

import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.amqp.transport.ReplyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.glutamate.util.concurrent.CloseableCompletableFuture;

/**
 * A request.
 * <p>
 * A request may be closed, which is a request to abort the request. The request
 * to abort is only processed locally and not propagated to the server.
 *
 * @param <R>
 *            the return type of the request
 */
public class Request<R> extends CloseableCompletableFuture<R> {

    private static final Logger logger = LoggerFactory.getLogger(Request.class);

    private final String service;
    private final Message message;
    private final ReplyHandler<R, Message> replyHandler;

    public Request(final String service, final Message message, final ReplyHandler<R, Message> replyHandler) {
        this.service = service;
        this.message = message;
        this.replyHandler = replyHandler;
    }

    public String getService() {
        return this.service;
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
        return String.format("[Request - %s, %s = %s]", this.service, this.message.getSubject(), this.message);
    }
}