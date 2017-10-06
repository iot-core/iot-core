package org.iotbricks.core.amqp.transport.internal;

import org.iotbricks.core.amqp.transport.AmqpTransport.Builder;
import org.iotbricks.core.amqp.transport.ReplyStrategy;

import io.vertx.core.Future;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonSender;

public interface RequestSender {
    public default void initialize(final Builder options) {
    }

    public Future<?> connected(ProtonConnection connection);

    public boolean isReady();

    public void sendRequest(ProtonSender sender, Request<?> request, ReplyStrategy replyStrategy);

}