package iotcore.service.device.binding;

import java.util.Optional;

import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.message.impl.MessageImpl;

import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonSender;
import iot.core.services.device.registry.serialization.AmqpByteSerializer;
import iot.core.services.device.registry.serialization.AmqpSerializer;
import iot.core.services.device.registry.serialization.jackson.JacksonSerializer;
import iot.core.utils.address.AddressProvider;
import iot.core.utils.address.DefaultAddressProvider;
import iotcore.service.device.AlwaysPassingDeviceSchemaValidator;
import iotcore.service.device.Device;
import iotcore.service.device.DeviceRegistry;
import iotcore.service.device.InMemoryDeviceRegistry;


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
            if("save".equals(msg.getProperties().getSubject())) {
                Device device = serializer.decode(msg.getBody(), Device.class);
                String deviceId = deviceRegistry.create(device);
                sendReply(sender, replyTo, deviceId);
            } else if("findById".equals(msg.getProperties().getSubject())) {
                String deviceId = serializer.decode(msg.getBody(), String.class);
                Optional<Device> device = deviceRegistry.findById(deviceId);
                sendReply(sender, replyTo, device.get());
            }
        }).open();
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
