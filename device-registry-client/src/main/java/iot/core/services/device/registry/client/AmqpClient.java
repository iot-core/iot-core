package iot.core.services.device.registry.client;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import io.vertx.core.Vertx;
import iot.core.services.device.registry.client.internal.util.AbstractAmqpClient;
import iot.core.services.device.registry.serialization.jackson.JacksonSerializer;
import iot.core.utils.address.DefaultAddressProvider;
import iotcore.service.device.Device;

public class AmqpClient extends AbstractAmqpClient {

    public static class Builder {

        private String hostname = "localhost";

        private int port = 5672;

        private String container;

        private Duration syncTimeout = Duration.ofSeconds(5);

        public Builder hostname(final String hostname) {
            this.hostname = hostname;
            return this;
        }

        public String hostname() {
            return this.hostname;
        }

        public Builder port(final int port) {
            this.port = port;
            return this;
        }

        public int port() {
            return this.port;
        }

        public Builder container(final String container) {
            this.container = container;
            return this;
        }

        public String container() {
            return this.container;
        }

        public Builder syncTimeout(final Duration syncTimeout) {
            this.syncTimeout = syncTimeout;
            return this;
        }

        public Duration syncTimeout() {
            return this.syncTimeout;
        }

        public Client build(final Vertx vertx) {
            return new AmqpClient(vertx, this.hostname, this.port, this.container, this.syncTimeout);
        }
    }

    public static Builder create() {
        return new Builder();
    }

    private AmqpClient(final Vertx vertx, final String hostname, final int port, final String container,
            final Duration syncTimeout) {
        super(vertx, hostname, port, container, JacksonSerializer.json(), new DefaultAddressProvider(),
                syncTimeout.abs().toMillis());
    }

    @Override
    protected CompletionStage<Optional<Device>> internalFindById(final String id) {
        return request("device", "findById", this.serializer.encode(id, null), bodyAsOptional(Device.class));
    }

    @Override
    protected CompletionStage<String> internalSave(final Device device) {
        return request("device", "save", this.serializer.encode(device, null), bodyAs(String.class));
    }

    @Override
    protected CompletionStage<String> internalCreate(final Device device) {
        return request("device", "create", this.serializer.encode(device, null), bodyAs(String.class));
    }

    @Override
    protected CompletionStage<Void> internalUpdate(final Device device) {
        return request("device", "create", this.serializer.encode(device, null), ignoreBody());
    }

}
