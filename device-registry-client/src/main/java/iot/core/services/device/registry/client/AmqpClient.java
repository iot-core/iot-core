package iot.core.services.device.registry.client;

import static iot.core.services.device.registry.serialization.AmqpByteSerializer.of;
import static iot.core.services.device.registry.serialization.jackson.JacksonSerializer.json;

import java.time.Duration;
import java.util.Optional;

import io.glutamate.util.concurrent.CloseableCompletionStage;
import io.vertx.core.Vertx;
import iot.core.amqp.transport.AmqpTransport;
import iot.core.service.device.Device;
import iot.core.services.device.registry.client.internal.AbstractDefaultClient;
import iot.core.services.device.registry.serialization.AmqpSerializer;
import iot.core.utils.address.DefaultAddressProvider;

public class AmqpClient extends AbstractDefaultClient {

    public static class Builder {

        private String hostname = "localhost";

        private int port = 5672;

        private String container;

        private Duration syncTimeout = Duration.ofSeconds(5);

        private Builder() {
        }

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

    private final AmqpSerializer serializer = of(json());

    private final AmqpTransport transport;

    private AmqpClient(final Vertx vertx, final String hostname, final int port, final String container,
            final Duration syncTimeout) {
        super(syncTimeout.abs());

        this.transport = new AmqpTransport(vertx, hostname, port, container, this.serializer,
                new DefaultAddressProvider());
    }

    @Override
    public void close() throws Exception {
        this.transport.close();
        super.close();
    }

    @Override
    protected CloseableCompletionStage<Optional<Device>> internalFindById(final String id) {
        return this.transport.request("device", "findById", id, this.transport.bodyAsOptional(Device.class));
    }

    @Override
    protected CloseableCompletionStage<String> internalSave(final Device device) {
        return this.transport.request("device", "save", device, this.transport.bodyAs(String.class));
    }

    @Override
    protected CloseableCompletionStage<String> internalCreate(final Device device) {
        return this.transport.request("device", "create", device, this.transport.bodyAs(String.class));
    }

    @Override
    protected CloseableCompletionStage<Void> internalUpdate(final Device device) {
        return this.transport.request("device", "update", device, this.transport.ignoreBody());
    }

}
