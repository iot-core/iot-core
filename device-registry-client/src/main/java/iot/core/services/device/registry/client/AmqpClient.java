package iot.core.services.device.registry.client;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import org.iotbricks.service.device.registry.api.Device;

import io.glutamate.util.concurrent.CloseableCompletionStage;
import io.vertx.core.Vertx;
import iot.core.amqp.transport.AmqpTransport;
import iot.core.services.device.registry.client.internal.AbstractAmqpClientBuilder;
import iot.core.services.device.registry.client.internal.AbstractDefaultClient;

public class AmqpClient extends AbstractDefaultClient {

    public static final class Builder extends AbstractAmqpClientBuilder<Builder> {

        public Builder(final AmqpTransport.Builder builder) {
            super(builder);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Client build(final Vertx vertx) {
            return new AmqpClient(vertx, new AmqpTransport.Builder(transport()), syncTimeout());
        }

    }

    public static Builder newClient() {
        return new Builder(AmqpTransport.newTransport());
    }

    public static Builder newClient(final AmqpTransport.Builder transport) {
        Objects.requireNonNull(transport);
        return new Builder(transport);
    }

    private final AmqpTransport transport;

    private AmqpClient(final Vertx vertx, final AmqpTransport.Builder transport, final Duration syncTimeout) {
        super(syncTimeout.abs());
        this.transport = transport.build(vertx);
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
