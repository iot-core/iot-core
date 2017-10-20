package org.iotbricks.core.amqp.transport.internal;

import org.iotbricks.core.amqp.transport.RequestInstance;
import org.iotbricks.core.amqp.transport.proton.Request;

import io.vertx.proton.ProtonSender;

public interface AmqpTransportContext<RQ extends Request> {
    public ProtonSender requestSender(RequestInstance<?, RQ> request);

    public void sendRequest(ProtonSender sender, RequestInstance<?, RQ> request);
}