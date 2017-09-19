package iotcore.service.device.binding;

import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonSender;
import iot.core.services.device.registry.serialization.Serializer;
import iot.core.services.device.registry.serialization.jackson.JacksonSerializer;
import iot.core.utils.address.AddressProvider;
import iot.core.utils.address.DefaultAddressProvider;
import iotcore.service.device.AlwaysPassingDeviceSchemaValidator;
import iotcore.service.device.Device;
import iotcore.service.device.DeviceRegistry;
import iotcore.service.device.InMemoryDeviceRegistry;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.util.Optional;


public class DeviceRegistryBinding {

    private final DeviceRegistry deviceRegistry;

    private final Serializer serializer = JacksonSerializer.json();
    
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
                Section body = msg.getBody();
                byte[] content = ((Data) body).getValue().asByteBuffer().array();
                Device device = serializer.decode(content, Device.class);
                String deviceId = deviceRegistry.create(device);
                sendReply(sender, replyTo, deviceId);
            } else if("findById".equals(msg.getProperties().getSubject())) {
                Section body = msg.getBody();
                byte[] content = ((Data) body).getValue().asByteBuffer().array();
                String deviceId = serializer.decode(content, String.class);
                Optional<Device> device = deviceRegistry.findById(deviceId);
                sendReply(sender, replyTo, device.get());
            }
        }).open();
    }

    void sendReply(ProtonSender sender, String replyTo, Object reply) {
        Message message = new MessageImpl();
        message.setBody(new Data(new Binary(serializer.encode(reply))));
        message.setAddress(replyTo);

        sender.send(message);
    }

    public static void main(String[] args) {
        new DeviceRegistryBinding(new InMemoryDeviceRegistry(new AlwaysPassingDeviceSchemaValidator())).start();
    }

}
