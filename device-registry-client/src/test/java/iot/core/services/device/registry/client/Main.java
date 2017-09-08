package iot.core.services.device.registry.client;

import iotcore.service.device.Device;

import java.util.HashMap;

public class Main {
    public static void main(final String[] args) throws Exception {

        try (Client client = FooBarClient.create()
                .endpoint("localhost:1234")
                .build()) {

            syncSave(client, "id1");
            syncFind(client, "id1");

            asyncSave(client, "id2");
            asyncFind(client, "id2");
        }

    }

    private static void syncSave(final Client client, final String id) {
        final Device device = new Device(id, new HashMap<>());
        final String result = client.sync().save(device);
        System.out.format("save[sync]: %s -> %s%n", device, result);
    }

    private static void syncFind(final Client client, final String id) {
        final Device result = client.sync().findById(id);
        System.out.format("find[sync]: %s -> %s%n", id, result);
    }

    private static void asyncSave(final Client client, final String id) {
        final Device device = new Device(id, new HashMap<>());
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
                                .map(device -> device.toString())
                                .orElse("<null>"));
            }
        });
    }
}
