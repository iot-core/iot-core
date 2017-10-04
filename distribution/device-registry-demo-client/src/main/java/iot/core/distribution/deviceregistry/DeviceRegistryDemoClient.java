package iot.core.distribution.deviceregistry;

import static io.vertx.core.Vertx.vertx;
import static java.util.UUID.randomUUID;

import org.iotbricks.client.device.registry.AmqpClient;
import org.iotbricks.client.device.registry.Client;
import org.iotbricks.service.device.registry.api.Device;

import com.google.common.collect.ImmutableMap;

import io.vertx.core.Vertx;

public final class DeviceRegistryDemoClient {

    private DeviceRegistryDemoClient() {
    }

    public static void main(final String[] args) throws Exception {
        Vertx vertx = null;
        try {
            vertx = vertx();
            Client client = AmqpClient.newClient().build(vertx);

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
