package org.iotbricks.core.amqp.transport;

import java.nio.ByteBuffer;

import io.glutamate.util.concurrent.CloseableCompletionStage;

public interface Transport<M> extends AutoCloseable {

    public interface RequestBuilder<R, RB extends RequestBuilder<R, RB>> {

        public RB builder();

        public default RB payload(final byte[] payload) {
            payload(ByteBuffer.wrap(payload));
            return builder();
        }

        public RB payload(ByteBuffer payload);

        public RB payload(String payload);

        public CloseableCompletionStage<R> execute();
    }

    public <R> RequestBuilder<R, ? extends RequestBuilder<R, ?>> newRequest(ResponseHandler<R, M> responseHandler);
}
