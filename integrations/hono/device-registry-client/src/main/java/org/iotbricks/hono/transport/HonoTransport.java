package org.iotbricks.hono.transport;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.amqp.transport.ResponseHandler;
import org.iotbricks.core.amqp.transport.proton.ProtonTransport;
import org.iotbricks.core.amqp.transport.proton.RequestSender;
import org.iotbricks.core.amqp.transport.proton.SharedClientAndRequestReceiverRequestSender;

import io.glutamate.lang.Resource;
import io.glutamate.util.concurrent.CloseableCompletionStage;
import io.vertx.core.Vertx;

public class HonoTransport extends ProtonTransport<HonoRequestInformation> {

    public static class Builder
            extends ProtonTransport.Builder<HonoRequestInformation, HonoTransport, Builder> {

        private Builder() {
        }

        private Builder(final Builder other) {
            super(other);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public HonoTransport build(final Resource<Vertx> vertx) {
            validate();
            return new HonoTransport(vertx, this);
        }

    }

    public static Builder newTransport() {
        return new Builder();
    }

    public static Builder newTransport(final Builder other) {
        Objects.requireNonNull(other);
        return new Builder(other);
    }

    protected HonoTransport(final Resource<Vertx> vertx,
            final Builder options) {
        super(vertx, HonoTransport::toAddress, options);

        open();
    }

    public static class HonoAmqpRequestBuilder<R>
            extends ProtonTransport.RequestBuilderImpl<R, HonoAmqpRequestBuilder<R>, HonoRequestInformation> {

        private String service;
        private String tenant;

        public HonoAmqpRequestBuilder(
                final Function<HonoAmqpRequestBuilder<R>, CloseableCompletionStage<R>> executor) {
            super(executor);
        }

        @Override
        public HonoAmqpRequestBuilder<R> builder() {
            return this;
        }

        public HonoAmqpRequestBuilder<R> service(final String service, final String tenant) {
            this.service = service;
            this.tenant = tenant;
            return builder();
        }

        @Override
        public HonoRequestInformation buildInformation() {
            return new HonoRequestInformation(this.service, this.tenant);
        }

    }

    @Override
    public <R> HonoAmqpRequestBuilder<R> newRequest(final ResponseHandler<R, Message> responseHandler) {
        return new HonoAmqpRequestBuilder<>(createRequestExecutor(responseHandler));
    }

    protected static String toAddress(final HonoRequestInformation information) {
        return String.format("%s/%s", information.getService(), information.getTenant());
    }

    protected static String toResponseAddress(final UUID uuid,
            final ProtonRequest<HonoRequestInformation> request) {

        final HonoRequestInformation information = request.getInformation();
        return String.format("%s/%s/%s", information.getService(), information.getTenant(), uuid);

    }

    public static RequestSender<ProtonRequest<HonoRequestInformation>> requestSender() {
        return SharedClientAndRequestReceiverRequestSender.uuid(HonoTransport::toResponseAddress);
    }

}
