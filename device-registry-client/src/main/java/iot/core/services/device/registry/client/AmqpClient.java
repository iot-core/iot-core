package iot.core.services.device.registry.client;

import static iot.core.services.device.registry.client.internal.util.Messages.bodyAsString;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import org.apache.qpid.proton.message.Message;

import io.vertx.core.Vertx;
import iot.core.services.device.registry.client.internal.util.Messages;
import iot.core.services.device.registry.serialization.Serializer;
import iot.core.services.device.registry.serialization.jackson.JacksonSerializer;
import iotcore.service.device.Device;

public class AmqpClient extends AbstractAmqpClient {

    public static class Builder {

        private String hostname = "localhost";

        private int port = 5672;

        private String container;

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

        public Client build(final Vertx vertx) {
            return new AmqpClient(vertx, this.hostname, this.port, this.container);
        }
    }

    public static Builder create() {
        return new Builder();
    }

    private final Serializer serializer = JacksonSerializer.json();

    private AmqpClient(final Vertx vertx, final String hostname, final int port, final String container) {
        super(vertx, hostname, port, container);
    }

    @Override
    protected CompletionStage<Optional<Device>> internalFindById(final String id) {
        return request("device", "findById", id, this::resultFindById);
    }

    @Override
    protected CompletionStage<String> internalSave(final Device device) {
        return request("device", "save", this.serializer.encode(device), Messages::bodyAsString);
    }

    private Optional<Device> resultFindById(final Message msg) {
        return Optional.ofNullable(bodyAsString(msg))
                .map(this.serializer::decodeDevice);
    }

}
