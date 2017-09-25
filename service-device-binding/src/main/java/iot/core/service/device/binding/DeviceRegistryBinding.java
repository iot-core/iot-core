package iot.core.service.device.binding;

import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.message.impl.MessageImpl;

import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonSender;
import iot.core.service.device.AlwaysPassingDeviceSchemaValidator;
import iot.core.service.device.Device;
import iot.core.service.device.DeviceRegistry;
import iot.core.service.device.InMemoryDeviceRegistry;
import iot.core.services.device.registry.serialization.AmqpByteSerializer;
import iot.core.services.device.registry.serialization.AmqpSerializer;
import iot.core.services.device.registry.serialization.jackson.JacksonSerializer;
import iot.core.utils.address.AddressProvider;
import iot.core.utils.address.DefaultAddressProvider;

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
        String address = addressProvider.requestAddress("device");

        connection.createReceiver(address).handler((delivery, msg) -> {
            ProtonSender sender = connection.createSender(null).open();
            String replyTo = msg.getReplyTo();
            String verb = msg.getProperties().getSubject();

            if (verb == null) {
                // FIXME: propagate exceptions
                throw new IllegalArgumentException(String.format("Verb missing"));
            }

            Object result = processRequest (verb, msg.getBody(), sender);
            sendReply(sender, replyTo, result);
        }).open();
    }

    private Object processRequest(String verb, Section body, ProtonSender sender) {
        switch (verb) {
        case "create": {
            Device device = serializer.decode(body, Device.class);
            return deviceRegistry.create(device);
        }
        case "save": {
            Device device = serializer.decode(body, Device.class);
            return deviceRegistry.save(device);
        }
        case "update": {
            Device device = serializer.decode(body, Device.class);
            deviceRegistry.update(device);
            return null;
        }
        case "findById": {
            String deviceId = serializer.decode(body, String.class);
            return deviceRegistry.findById(deviceId);
        }
        default:
            throw new IllegalArgumentException(String.format("Unsupported verb: %s" + verb));
        }
    }

    void sendReply(ProtonSender sender, String replyTo, Object reply) {
        Message message = new MessageImpl();
        message.setBody(serializer.encode(reply));
        message.setAddress(replyTo);

        sender.send(message);
    }

    public static void main(String[] args) {
        new DeviceRegistryBinding(new InMemoryDeviceRegistry(new AlwaysPassingDeviceSchemaValidator())).start();
    }

}
