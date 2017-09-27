package iot.core.service.device.binding;

import static iot.core.utils.binding.ErrorCondition.DECODE_ERROR;

import java.util.Optional;

import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonSender;
import iot.core.service.binding.amqp.AmqpRejectResponseHandler;
import iot.core.service.binding.amqp.AmqpRequestContext;
import iot.core.service.binding.common.DefaultErrorTranslator;
import iot.core.service.binding.common.MessageResponseHandler;
import iot.core.service.binding.proton.ProtonRequestContext;
import iot.core.service.binding.proton.ProtonRequestProcessor;
import iot.core.service.device.AlwaysPassingDeviceSchemaValidator;
import iot.core.service.device.Device;
import iot.core.service.device.DeviceRegistry;
import iot.core.service.device.InMemoryDeviceRegistry;
import iot.core.services.device.registry.serialization.AmqpByteSerializer;
import iot.core.services.device.registry.serialization.AmqpSerializer;
import iot.core.services.device.registry.serialization.jackson.JacksonSerializer;
import iot.core.utils.address.AddressProvider;
import iot.core.utils.address.DefaultAddressProvider;
import iot.core.utils.binding.RequestException;

public class DeviceRegistryBinding {

    private final DeviceRegistry deviceRegistry;

    private final AmqpSerializer serializer = AmqpByteSerializer.of(JacksonSerializer.json());

    private AddressProvider addressProvider = new DefaultAddressProvider();

    public DeviceRegistryBinding(DeviceRegistry deviceRegistry) {
        this.deviceRegistry = deviceRegistry;
    }

    public void start() {
        Vertx vertx = Vertx.vertx();
        ProtonClient client = ProtonClient.create(vertx);
        client.connect("localhost", 5672, connectionResult -> {
            if (connectionResult.succeeded()) {
                ProtonConnection connection = connectionResult.result();
                bindServiceToConnection(connection);
            } else {
                connectionResult.cause().printStackTrace();
            }
        });
    }

    private void bindServiceToConnection(ProtonConnection connection) {
        connection.open();

        final String address = addressProvider.requestAddress("device");

        final ProtonSender sender = connection.createSender(null);
        sender.openHandler(senderReady -> {

            final ProtonRequestProcessor processor = new ProtonRequestProcessor(
                    this.serializer,
                    sender,
                    new MessageResponseHandler<>(AmqpRequestContext::getReplyToAddress),
                    new AmqpRejectResponseHandler(),
                    new DefaultErrorTranslator(),
                    this::processRequest);

            connection
                    .createReceiver(address)
                    .handler(processor.messageHandler())
                    .open();
        })

                .open();
    }

    private Object processRequest(final ProtonRequestContext context) throws Exception {

        final Optional<String> verb = context.getVerb();

        if (!verb.isPresent()) {
            throw new RequestException(DECODE_ERROR, "Verb missing");
        }

        switch (verb.get()) {
        case "create": {
            Device device = context.decodeRequest(Device.class);
            return deviceRegistry.create(device);
        }
        case "save": {
            Device device = context.decodeRequest(Device.class);
            return deviceRegistry.save(device);
        }
        case "update": {
            Device device = context.decodeRequest(Device.class);
            deviceRegistry.update(device);
            return null;
        }
        case "findById": {
            String deviceId = context.decodeRequest(String.class);
            return deviceRegistry.findById(deviceId);
        }
        default:
            throw new RequestException(DECODE_ERROR, String.format("Unsupported verb: %s", verb));
        }
    }

    public static void main(String[] args) {
        new DeviceRegistryBinding(new InMemoryDeviceRegistry(new AlwaysPassingDeviceSchemaValidator())).start();
    }

}
