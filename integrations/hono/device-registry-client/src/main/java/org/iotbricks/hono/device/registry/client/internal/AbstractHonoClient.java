package org.iotbricks.hono.device.registry.client.internal;

import static io.glutamate.util.Optionals.presentAndEqual;
import static org.iotbricks.core.amqp.transport.utils.Properties.status;

import java.util.Optional;
import java.util.function.Consumer;

import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.amqp.transport.ResponseHandler;
import org.iotbricks.core.utils.serializer.StringSerializer;
import org.iotbricks.hono.transport.HonoTransport;
import org.iotbricks.hono.transport.HonoTransport.HonoAmqpRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHonoClient implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHonoClient.class);

    public static abstract class Builder<B extends Builder<B>> {

        private HonoTransport.Builder transport;

        private String tenant;

        public Builder() {
            this.transport = HonoTransport.newTransport()
                    .requestSenderFactory(HonoTransport::requestSender);
        }

        protected abstract B builder();

        public Builder(final B other) {
            this.transport = HonoTransport.newTransport(other.transport());
            this.tenant = other.tenant();
        }

        public B tenant(final String tenant) {
            this.tenant = tenant;
            return builder();
        }

        public String tenant() {
            return this.tenant;
        }

        public B transport(final HonoTransport.Builder transport) {
            this.transport = transport;
            return builder();
        }

        public HonoTransport.Builder transport() {
            return this.transport;
        }

        public B transport(final Consumer<HonoTransport.Builder> transportCustomizer) {
            transportCustomizer.accept(this.transport);
            return builder();
        }

    }

    private final HonoTransport transport;
    protected final StringSerializer serializer;
    private final String tenant;
    private final String service;

    public AbstractHonoClient(final String service, final String tenant, final StringSerializer serializer,
            final HonoTransport transport) {
        this.service = service;
        this.tenant = tenant;
        this.serializer = serializer;
        this.transport = transport;

    }

    @Override
    public void close() throws Exception {
        this.transport.close();
    }

    protected <T> HonoAmqpRequestBuilder<T> request(final String subject,
            final ResponseHandler<T, Message> handler) {

        return this.transport.newRequest(handler)
                .service(this.service, this.tenant)
                .subject(subject)
                .rejected((rejected, request) -> request.fail("Request rejected by server"));
    }

    protected <T> HonoAmqpRequestBuilder<T> request(final String subject, final Object payload,
            final ResponseHandler<T, Message> handler) {

        final String data = this.serializer.encodeString(payload);

        logger.debug("Request - subject: {}, payload: {}", subject, data);

        return request(subject, handler)
                .payload(data);
    }

    protected <T> T success(final Message reply, final int successCode, final Class<T> clazz) {

        final Optional<Integer> status = status(reply);

        if (!presentAndEqual(status, successCode)) {
            throw unwrapError(status, reply);
        }

        return this.serializer.decodeString((String) ((AmqpValue) reply.getBody()).getValue(), clazz);
    }

    protected static RuntimeException unwrapError(final Optional<Integer> status, final Message reply) {
        return status
                .map(v -> new RuntimeException(String.format("Remote service error: %s", v)))
                .orElseGet(() -> new RuntimeException("Status code missing in response"));
    }

}