package org.iotbricks.hono.device.registry.client.internal;

import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.amqp.transport.ResponseHandler;
import org.iotbricks.core.amqp.transport.proton.SharedReceiverRequestSender;
import org.iotbricks.core.utils.serializer.StringSerializer;
import org.iotbricks.hono.device.registry.client.transport.HonoTransport;
import org.iotbricks.hono.device.registry.client.transport.HonoTransport.HonoAmqpRequestBuilder;

import io.vertx.core.Vertx;

public abstract class AbstractHonoClient implements AutoCloseable {

    private final HonoTransport transport;
    protected final StringSerializer serializer;
    private final String tenant;

    public AbstractHonoClient(final Vertx vertx, final String tenant, final StringSerializer serializer) {

        this.tenant = tenant;
        this.serializer = serializer;

        this.transport = HonoTransport.newTransport()
                .requestSenderFactory(SharedReceiverRequestSender::uuid)
                .build(vertx);

    }

    protected <T> HonoAmqpRequestBuilder<T> request(final String subject,
            final ResponseHandler<T, Message> handler) {

        return this.transport.newRequest(handler)
                .service("registration", this.tenant)
                .subject(subject)
                .rejected((rejected, request) -> request.fail("Request rejected by server"));
    }

    protected <T> HonoAmqpRequestBuilder<T> request(final String subject, final Object payload,
            final ResponseHandler<T, Message> handler) {

        return request(subject, handler)
                .payload(this.serializer.encodeString(payload));
    }

    @Override
    public void close() throws Exception {
        this.transport.close();
    }

}