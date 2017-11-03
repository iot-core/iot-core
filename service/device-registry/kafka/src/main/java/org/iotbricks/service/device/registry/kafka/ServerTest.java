package org.iotbricks.service.device.registry.kafka;

import io.debezium.kafka.KafkaCluster;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonHelper;
import io.vertx.proton.ProtonSender;
import org.apache.qpid.proton.message.Message;
import org.iotbricks.service.device.registry.api.Device;
import org.iotbricks.service.device.registry.api.DeviceRegistryService;
import org.iotbricks.service.device.registry.inmemory.InMemoryDeviceRegistryService;
import org.iotbricks.service.device.registry.spi.AlwaysPassingDeviceSchemaValidator;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ServerTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        // Embedded Kafka cluster
        new KafkaCluster().withPorts(2181, 9092).
                usingDirectory(new File("/tmp/iotbricks-kafka")).deleteDataPriorToStartup(true).
                addBrokers(1).startup();

        // Services
        DeviceRegistryService deviceRegistryService = new InMemoryDeviceRegistryService(new AlwaysPassingDeviceSchemaValidator());
        new DeviceRegistryServiceEventBusProducer().start();
        new DeviceRegistryServiceEventBusConsumer(deviceRegistryService).start();
        new RequestReplyHandler(deviceRegistryService).start();

        // Test
        ProtonClient.create(Vertx.vertx()).connect("localhost", 5672, connection -> {
            ProtonSender sender = connection.result().open().createSender("service.device.registry").open();

            Device device = new Device();
            device.setDeviceId("myDevice");
            String deviceJson = Json.encode(device);
            Message msg = ProtonHelper.message(deviceJson);
            msg.setSubject("save");
            sender.send(msg);

            msg = ProtonHelper.message(device.getDeviceId());
            msg.setSubject("remove");
            sender.send(msg);

            ProtonSender requestReplySender = connection.result().open().createSender("service.device.registry.requests").open();
            msg = ProtonHelper.message(device.getDeviceId());
            msg.setSubject("findById");
            msg.setReplyTo(UUID.randomUUID().toString());
            requestReplySender.send(msg);
        });
    }

}
