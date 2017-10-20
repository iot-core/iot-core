package org.iotbricks.hono.device.registry.client.internal;

import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.amqp.transport.ResponseHandler;
import org.iotbricks.core.utils.serializer.StringSerializer;
import org.iotbricks.hono.transport.HonoTransport;
import org.iotbricks.hono.transport.HonoTransport.HonoAmqpRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHonoClient implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHonoClient.class);

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

    @Override
    public void close() throws Exception {
        this.transport.close();
    }

}