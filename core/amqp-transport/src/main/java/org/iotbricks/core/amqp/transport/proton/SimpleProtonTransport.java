package org.iotbricks.core.amqp.transport.proton;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.function.Function;

import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.amqp.transport.ResponseHandler;

import io.glutamate.lang.Resource;
import io.glutamate.util.concurrent.CloseableCompletionStage;
import io.vertx.core.Vertx;

/**
 * A simple proton based AMQP transport.
 * <p>
 * This transport assumes that there is a single remote address per request to
 * send requests to.
 */
public class SimpleProtonTransport extends ProtonTransport<SimpleProtonTransport.RequestInformation> {

    public static class RequestInformation {
        private final String address;

        public RequestInformation(final String address) {
            this.address = address;
        }

        public String getAddress() {
            return this.address;
        }
    }

    public static class Builder extends ProtonTransport.Builder<RequestInformation, SimpleProtonTransport, Builder> {

        protected Builder() {

        }

        protected Builder(final Builder other) {
            super(other);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public SimpleProtonTransport build(final Resource<Vertx> vertx) {
            Objects.requireNonNull(vertx);
            return new SimpleProtonTransport(vertx, this);
        }

    }

    protected static class RequestBuilder<R>
            extends ProtonTransport.RequestBuilderImpl<R, RequestBuilder<R>, RequestInformation> {

        private String address;

        public RequestBuilder(final Function<RequestBuilder<R>, CloseableCompletionStage<R>> executor) {
            super(executor);
        }

        @Override
        public RequestBuilder<R> builder() {
            return this;
        }

        public RequestBuilder<R> address(final String address) {
            Objects.requireNonNull(address);

            this.address = address;
            return builder();
        }

        public String address() {
            return this.address;
        }

    }

    public static Builder newTransport() {
        return new Builder();
    }

    public static Builder newTransport(final Builder other) {
        return new Builder(other);
    }

    protected SimpleProtonTransport(final Resource<Vertx> vertx, final Builder options) {
        super(vertx, RequestInformation::getAddress, options);
    }

    @Override
    protected <R> CloseableCompletionStage<R> executeRequest(
            final RequestBuilderImpl<R, ?, RequestInformation> requestBuilder,
            final ResponseHandler<R, Message> responseHandler) {

        final RequestInformation ri = requestBuilder.buildInformation();

        requireNonNull(ri);
        requireNonNull(ri.getAddress());

        return super.executeRequest(requestBuilder, responseHandler);
    }

    @Override
    public <R> RequestBuilder<R> newRequest(final ResponseHandler<R, Message> responseHandler) {
        return new RequestBuilder<>(createRequestExecutor(responseHandler));
    }

}
