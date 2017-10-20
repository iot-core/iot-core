package org.iotbricks.hono.device.registry.client;

import java.util.Map;
import java.util.Objects;

import org.iotbricks.core.serialization.jackson.JacksonSerializer;
import org.iotbricks.hono.client.AbstractHonoClient;
import org.iotbricks.hono.device.registry.client.model.DeviceInformation;
import org.iotbricks.hono.transport.HonoTransport;

import io.glutamate.util.concurrent.CloseableCompletionStage;
import io.vertx.core.Vertx;

public class AmqpClient extends AbstractHonoClient implements Client {

    public static class Builder extends AbstractHonoClient.Builder<Builder> {

        public Builder() {
        }

        public Builder(final Builder other) {
            super(other);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Client build(final Vertx vertx) {

            Objects.requireNonNull(vertx, "'vertx' must not be null");

            validate();

            return new AmqpClient(vertx, this.tenant(), this.transport());
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

}
