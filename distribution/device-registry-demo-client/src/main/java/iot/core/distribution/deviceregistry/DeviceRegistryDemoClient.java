package iot.core.distribution.deviceregistry;

import com.google.common.collect.ImmutableMap;
import io.vertx.core.Vertx;
import iot.core.services.device.registry.client.AmqpClient;
import iot.core.services.device.registry.client.Client;
import iot.core.service.device.Device;

import static io.vertx.core.Vertx.vertx;
import static java.util.UUID.randomUUID;

public class DeviceRegistryDemoClient {

    public static void main(final String[] args) throws Exception {
        Vertx vertx = null;
        try {
            vertx = vertx();
            Client client = AmqpClient.create().build(vertx);

            String deviceId = randomUUID().toString();
            Device device = new Device();
            device.setDeviceId(deviceId);
            device.setProperties(ImmutableMap.of("customProperty1", "Shouts for Kapua! ;)"));

            System.out.println("Saving device...");
            client.sync().save(device);

            System.out.println("Loading device state...");
            device = client.sync().findById(deviceId).get();
            System.out.println(device);
        } finally {
            vertx.close();
        }
    }

}
