package org.iotbricks.core.amqp.transport;

import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.amqp.transport.proton.ReplyStrategy;
import org.iotbricks.core.amqp.transport.proton.Request;

import io.glutamate.util.concurrent.CloseableCompletableFuture;
import io.vertx.proton.ProtonDelivery;

/**
 * A request instance.
 * <p>
 * A request may be closed, which is a request to abort the request. The request
 * to abort is only processed locally and not propagated to the server.
 *
 * @param <R>
 *            the return type of the request
 */
public class RequestInstance<R, RQ extends Request> extends CloseableCompletableFuture<R> {

    private final String address;
    private final RQ request;
    private final ReplyStrategy<R, RQ> replyStrategy;

    public RequestInstance(final String address, final RQ request, final ReplyStrategy<R, RQ> replyStrategy) {
        this.address = address;
        this.request = request;
        this.replyStrategy = replyStrategy;
    }

    public String getAddress() {
        return this.address;
    }

    public Message getMessage() {
        return this.request.getMessage();
    }

    public ReplyStrategy<R, RQ> getReplyStrategy() {
        return this.replyStrategy;
    }

    public RQ getRequest() {
        return this.request;
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

    public void handleDelivery(final ProtonDelivery delivery) {
        this.replyStrategy.handleDelivery(this, delivery);
    }

    public void handleResponse(final Message response) {
        this.replyStrategy.handleResponse(this, response);
    }

    @Override
    public String toString() {
        return String.format("[RequestInstance: %s - %s]", this.address, this.request);
    }
}