package org.iotbricks.hono.device.registry.client;

import static io.glutamate.util.Optionals.presentAndEqual;
import static org.iotbricks.core.amqp.transport.utils.Properties.status;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.serialization.jackson.JacksonSerializer;
import org.iotbricks.hono.device.registry.client.internal.AbstractHonoClient;
import org.iotbricks.hono.device.registry.client.model.DeviceInformation;
import org.iotbricks.hono.transport.HonoTransport;

import io.glutamate.util.concurrent.CloseableCompletionStage;
import io.vertx.core.Vertx;

public class AmqpClient extends AbstractHonoClient implements Client {

    public static class Builder {

        private HonoTransport.Builder transport;

        private String tenant;

        public Builder() {
            this.transport = HonoTransport.newTransport()
                    .requestSenderFactory(HonoTransport::requestSender);
        }

        public Builder builder() {
            return this;
        }

        public Builder(final Builder other) {
            this.transport = HonoTransport.newTransport(other.transport);
            this.tenant = other.tenant;
        }

        public Builder tenant(final String tenant) {
            this.tenant = tenant;
            return builder();
        }

        public String tenant() {
            return this.tenant;
        }

        public Builder transport(final HonoTransport.Builder transport) {
            this.transport = transport;
            return builder();
        }

        public HonoTransport.Builder transport() {
            return this.transport;
        }

        public Builder transport(final Consumer<HonoTransport.Builder> transportCustomizer) {
            transportCustomizer.accept(this.transport);
            return builder();
        }

        public AmqpClient build(final Vertx vertx) {
            Objects.requireNonNull(this.tenant, "'tenant' must not be null");
            return new AmqpClient(vertx, this.tenant, this.transport);
        }

    }

    public static Builder newClient() {
        return new Builder();
    }

    private AmqpClient(final Vertx vertx, final String tenant, final HonoTransport.Builder transport) {
        super("registration", tenant, JacksonSerializer.json(), transport.build(vertx));
    }

    @Override
    public CloseableCompletionStage<DeviceInformation> registerDevice(final String deviceId,
            final Map<String, ?> data) {

        return request("register", data,
                reply -> success(reply, 201, DeviceInformation.class))
                        .applicationProperty("device_id", deviceId)
                        .execute();

    }

    @Override
    public CloseableCompletionStage<DeviceInformation> getDevice(final String deviceId) {

        return request("get",
                reply -> success(reply, 200, DeviceInformation.class))
                        .applicationProperty("device_id", deviceId)
                        .execute();

    }

    private <T> T success(final Message reply, final int successCode, final Class<T> clazz) {

        final Optional<Integer> status = status(reply);

        if (!presentAndEqual(status, successCode)) {
            throw unwrapError(status, reply);
        }

        return this.serializer.decodeString((String) ((AmqpValue) reply.getBody()).getValue(), clazz);
    }

    private static RuntimeException unwrapError(final Optional<Integer> status, final Message reply) {
        return status
                .map(v -> new RuntimeException(String.format("Remote service error: %s", v)))
                .orElseGet(() -> new RuntimeException("Status code missing in response"));
    }

}
