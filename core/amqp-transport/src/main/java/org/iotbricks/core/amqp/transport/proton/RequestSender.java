package org.iotbricks.core.amqp.transport.proton;

import org.iotbricks.core.amqp.transport.RequestInstance;

import io.vertx.core.Future;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonSender;

public interface RequestSender<RQ extends Request> {

    public default void initialize(final RequestSenderContext<RQ> context) {
    }

    public Future<?> connected(ProtonConnection connection);

    public boolean prepareRequest(RequestInstance<?, RQ> request);

    public void sendRequest(ProtonSender sender, RequestInstance<?, RQ> request);

}