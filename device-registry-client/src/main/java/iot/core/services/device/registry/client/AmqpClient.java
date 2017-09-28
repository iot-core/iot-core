package iot.core.services.device.registry.client;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import org.iotbricks.service.device.registry.api.Device;

import io.glutamate.util.concurrent.CloseableCompletionStage;
import io.vertx.core.Vertx;
import iot.core.amqp.transport.AmqpTransport;
import iot.core.services.device.registry.client.internal.AbstractDefaultClient;

public class AmqpClient extends AbstractDefaultClient {

    public static class Builder {

        private AmqpTransport.Builder transport;

        private Duration syncTimeout = Duration.ofSeconds(5);

        private Builder(final AmqpTransport.Builder transport) {
            this.transport = transport;
        }

        public Builder transport(final AmqpTransport.Builder transport) {
            this.transport = transport;
            return this;
        }

        public AmqpTransport.Builder transport() {
            return this.transport;
        }

        public Builder hostname(final String hostname) {
            this.transport.hostname(hostname);
            return this;
        }

        public String hostname() {
            return this.transport.hostname();
        }

        public Builder username(final String username) {
            this.transport.username(username);
            return this;
        }

        public String username() {
            return this.transport.username();
        }

        public Builder password(final String password) {
            this.transport.password(password);
            return this;
        }

        public String password() {
            return this.transport.password();
        }

        public Builder port(final int port) {
            this.transport.port(port);
            return this;
        }

        public int port() {
            return this.transport.port();
        }

        public Builder syncTimeout(final Duration syncTimeout) {
            this.syncTimeout = syncTimeout;
            return this;
        }

        public Duration syncTimeout() {
            return this.syncTimeout;
        }

        public Client build(final Vertx vertx) {
            return new AmqpClient(vertx, new AmqpTransport.Builder(this.transport), this.syncTimeout);
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
