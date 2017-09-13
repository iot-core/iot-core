package iot.core.services.device.registry.client;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import iotcore.service.device.AlwaysPassingDeviceSchemaValidator;
import iotcore.service.device.Device;
import iotcore.service.device.InMemoryDeviceRegistry;

public class Main {

    private static Client createClient(final boolean local) {
        if (!local) {
            return FooBarClient.create()
                    .endpoint("localhost:1234")
                    .build();
        } else {
            return new LocalClient(new InMemoryDeviceRegistry(new AlwaysPassingDeviceSchemaValidator()));
        }
    }

    public static void main(final String[] args) throws Exception {

        try (Client client = createClient(true)) {

            syncSave(client, "id1");
            syncFind(client, "id1");

            asyncSave(client, "id2");
            asyncFind(client, "id2");
        }

    }

    private static void syncSave(final Client client, final String id) {
        final Device device = createNewDevice(id);

        final String result = client.sync().save(device);
        System.out.format("save[sync]: %s -> %s%n", device, result);
    }

    private static void syncFind(final Client client, final String id) {
        final Optional<Device> result = client.sync().findById(id);
        System.out.format("find[sync]: %s -> %s%n", id,
                result
                        .map(Object::toString)
                        .orElse("<null>"));
    }

    private static void asyncSave(final Client client, final String id) {
        final Device device = createNewDevice(id);

        client.async().save(device).whenComplete((result, error) -> {
            if (error != null) {
                System.err.println("save[async]: operation failed");
                error.printStackTrace();
            } else {
                System.out.format("save[async]: %s -> %s%n", device, result);
            }
        });
    }

    private static void asyncFind(final Client client, final String id) {
        client.async().findById(id).whenComplete((result, error) -> {
            if (error != null) {
                System.err.println("find[async]: operation failed");
                error.printStackTrace();
            } else {
                System.out.format("find[async]: %s -> %s%n", id,
                        result
                                .map(Object::toString)
                                .orElse("<null>"));
            }
        });
    }

    private static Device createNewDevice(final String id) {
        final Date now = new Date();
        final Device device = new Device(id, now, now, "my:device", new HashMap<>());
        return device;
    }
}
