package org.iotbricks.core.amqp.transport.proton;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.amqp.transport.AmqpTransport;
import org.iotbricks.core.amqp.transport.RequestInstance;
import org.iotbricks.core.amqp.transport.ResponseHandler;
import org.iotbricks.core.amqp.transport.proton.ProtonTransport.ProtonRequest;

import io.glutamate.util.concurrent.CloseableCompletionStage;
import io.vertx.core.Vertx;
import io.vertx.proton.ProtonDelivery;

public abstract class ProtonTransport<RI>
        extends AbstractAmqpTransport<ProtonRequest<RI>>
        implements AmqpTransport<Message> {

    protected static abstract class Builder<RI, C extends ProtonTransport<RI>, B extends Builder<RI, C, B>>
            extends AbstractAmqpTransport.Builder<ProtonRequest<RI>, ProtonTransport<?>, B> {

        protected Builder() {
        }

        protected Builder(final B other) {
            super(other);
        }
    }

    public abstract static class RequestBuilderImpl<R, RB extends RequestBuilderImpl<R, RB, RI>, RI>
            implements AmqpRequestBuilder<R, RB> {

        private final Function<RB, CloseableCompletionStage<R>> executor;

        protected final Message message;

        protected BiConsumer<Rejected, RequestInstance<?, ?>> rejectedConsumer;

        public RequestBuilderImpl(
                final Function<RB, CloseableCompletionStage<R>> executor) {
            this.executor = executor;
            this.message = Message.Factory.create();
        }

        @Override
        public CloseableCompletionStage<R> execute() {
            return this.executor.apply(builder());
        }

        protected Message getMessage() {
            return this.message;
        }

        public RB body(final Section body) {
            this.message.setBody(body);
            return builder();
        }

        public RB payload(final Section body) {
            this.message.setBody(body);
            return builder();
        }

        @Override
        public RB payload(final ByteBuffer payload) {
            this.message.setBody(new Data(Binary.create(payload)));
            return builder();
        }

        @Override
        public RB payload(final String payload) {
            this.message.setBody(new AmqpValue(payload));
            return builder();
        }

        @Override
        public RB subject(final String subject) {
            this.message.setSubject(subject);
            return builder();
        }

        @SuppressWarnings("unchecked")
        @Override
        public RB applicationProperty(final String key, final Object value) {
            ApplicationProperties properties = this.message.getApplicationProperties();
            if (properties == null) {
                properties = new ApplicationProperties(new HashMap<>());
            }
            properties.getValue().put(key, value);
            return builder();
        }

        @Override
        public RB rejected(final BiConsumer<Rejected, RequestInstance<?, ?>> consumer) {
            this.rejectedConsumer = consumer;
            return builder();
        }

        public RI buildInformation() {
            return null;
        }
    }

    protected ProtonTransport(final Vertx vertx, final Function<RI, String> addressProvider,
            final Builder<RI, ? extends ProtonTransport<?>, ?> options) {
        super(vertx, request -> addressProvider.apply(request.getInformation()), options);
    }

    public static class ProtonRequest<RI> implements Request {

        private final Message message;
        private final RI information;

        protected ProtonRequest(final Message message, final RI information) {
            this.message = message;
            this.information = information;
        }

        @Override
        public Message getMessage() {
            return this.message;
        }

        public RI getInformation() {
            return this.information;
        }

    }

    protected <R> CloseableCompletionStage<R> executeRequest(final RequestBuilderImpl<R, ?, RI> requestBuilder,
            final ResponseHandler<R, Message> responseHandler) {

        final BiConsumer<Rejected, RequestInstance<?, ?>> rejectedConsumer = requestBuilder.rejectedConsumer;
        final Message message = requestBuilder.getMessage();
        final RI information = requestBuilder.buildInformation();

        return request(
                new ProtonRequest<>(message, information),
                new ReplyStrategy<R, ProtonRequest<RI>>() {

                    @Override
                    public void handleDelivery(final RequestInstance<R, ProtonRequest<RI>> request,
                            final ProtonDelivery delivery) {

                        final DeliveryState state = delivery.getRemoteState();

                        if (state instanceof Rejected) {
                            if (rejectedConsumer != null) {
                                rejectedConsumer.accept((Rejected) state, request);
                            }
                        }
                    }

                    @Override
                    public void handleResponse(final RequestInstance<R, ProtonRequest<RI>> request,
                            final Message message) {
                        try {
                            final R result = responseHandler.response(message);
                            request.complete(result);
                        } catch (final Exception e) {
                            request.fail(e);
                        }
                    }
                });

    }

    protected <R, RB extends RequestBuilderImpl<R, RB, RI>> Function<RB, CloseableCompletionStage<R>> createRequestExecutor(
            final ResponseHandler<R, Message> responseHandler) {
        return requestBuilder -> executeRequest(requestBuilder, responseHandler);
    }
}
