package org.iotbricks.core.amqp.transport;

import java.util.Optional;

import org.apache.qpid.proton.message.Message;

import io.glutamate.util.concurrent.CloseableCompletionStage;

public interface Transport<M> extends AutoCloseable {

    public <R> CloseableCompletionStage<R> request(String address, String verb, Object[] request,
            ReplyHandler<R, Message> replyHandler);

    public <R> CloseableCompletionStage<R> request(String service, String verb, Object request,
            ReplyHandler<R, Message> replyHandler);

    public ReplyHandler<Void, M> ignoreBody();

    public <T> ReplyHandler<T, M> bodyAs(final Class<T> clazz);

    public <T> ReplyHandler<Optional<T>, M> bodyAsOptional(final Class<T> clazz);

}
