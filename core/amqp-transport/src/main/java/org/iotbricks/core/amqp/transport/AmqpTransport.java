package org.iotbricks.core.amqp.transport;

import java.util.function.BiConsumer;

import org.apache.qpid.proton.amqp.messaging.Rejected;

public interface AmqpTransport<M> extends Transport<M> {

    public interface AmqpRequestBuilder<R, RB extends AmqpRequestBuilder<R, RB>>
            extends RequestBuilder<R, RB> {
        public RB subject(String subject);

        public RB applicationProperty(String key, Object value);

        public RB rejected(BiConsumer<Rejected, RequestInstance<?, ?>> consumer);
    }

    @Override
    public <R> AmqpRequestBuilder<R, ? extends AmqpRequestBuilder<R, ?>> newRequest(
            ResponseHandler<R, M> responseHandler);
}
