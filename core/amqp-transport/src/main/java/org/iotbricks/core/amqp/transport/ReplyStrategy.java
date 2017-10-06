package org.iotbricks.core.amqp.transport;

import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.amqp.transport.AmqpTransport.Request;

import io.vertx.proton.ProtonDelivery;

public interface ReplyStrategy {

    public void handleDelivery(Request<?> request, ProtonDelivery delivery);

    public void handleResponse(Request<?> request, Message message);

}
