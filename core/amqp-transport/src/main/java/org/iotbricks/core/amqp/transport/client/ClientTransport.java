package org.iotbricks.core.amqp.transport.client;

import java.util.Optional;

import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.amqp.transport.ResponseHandler;

import io.glutamate.util.concurrent.CloseableCompletionStage;

public interface ClientTransport<M> extends AutoCloseable {

    public <R> CloseableCompletionStage<R> request(String address, String verb, Object[] request,
            ResponseHandler<R, Message> replyHandler);

    public <R> CloseableCompletionStage<R> request(String service, String verb, Object request,
            ResponseHandler<R, Message> replyHandler);

    public ResponseHandler<Void, M> ignoreBody();

    public <T> ResponseHandler<T, M> bodyAs(final Class<T> clazz);

    public <T> ResponseHandler<Optional<T>, M> bodyAsOptional(final Class<T> clazz);

}
