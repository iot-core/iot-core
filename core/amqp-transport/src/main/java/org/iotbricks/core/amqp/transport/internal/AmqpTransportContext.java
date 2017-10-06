package org.iotbricks.core.amqp.transport.internal;

import io.vertx.proton.ProtonSender;

public interface AmqpTransportContext {
    public ProtonSender requestSender(String service);

    public void sendRequest(ProtonSender sender, Request<?> request);
}