package org.iotbricks.core.amqp.transport.proton;

import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.amqp.transport.RequestInstance;

import io.vertx.proton.ProtonDelivery;

public interface ReplyStrategy<R, RQ extends Request> {

    public void handleDelivery(RequestInstance<R, RQ> request, ProtonDelivery delivery);

    public void handleResponse(RequestInstance<R, RQ> request, Message message);

}
