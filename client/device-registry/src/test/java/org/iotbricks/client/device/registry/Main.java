package org.iotbricks.client.device.registry;

import static org.iotbricks.core.serialization.jackson.JacksonSerializer.json;

import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.iotbricks.service.device.registry.api.Device;
import org.iotbricks.service.device.registry.inmemory.InMemoryDeviceRegistryService;
import org.iotbricks.service.device.registry.spi.AlwaysPassingDeviceSchemaValidator;

import io.vertx.core.Vertx;

public class Main {

    static Client createLocalClient() {
        return new LocalClient(new InMemoryDeviceRegistryService(new AlwaysPassingDeviceSchemaValidator()));
    }

    static Client createAmqpClient(final Vertx vertx) {
        return AmqpClient.newClient()
                .serializer(json())
                .build(vertx);
    }

    public static void main(final String[] args) throws Exception {

        final Vertx vertx = Vertx.vertx();

        try (final Client client = createAmqpClient(vertx)) {

            asyncSave(client, "id2");
            asyncFind(client, "id2");

            syncCreate(client, "id1");
            syncSave(client, "id1");
            syncUpdate(client, "id1");
            syncFind(client, "id1");

            Thread.sleep(1_000);

        } finally {
            vertx.close();
        }

    }

    private static void syncCreate(final Client client, final String id) {
        final Device device = createNewDevice(id);

        final String result = client.sync().create(device);
        System.out.format("create[sync]: %s -> %s%n", device, result);
    }

    private static void syncSave(final Client client, final String id) {
        final Device device = createNewDevice(id);

        final String result = client.sync().save(device);
        System.out.format("save[sync]: %s -> %s%n", device, result);
    }

    private static void syncUpdate(final Client client, final String id) {
        final Device device = createNewDevice(id);

        client.sync().update(device);
        System.out.format("update[sync]: %s%n", device);
    }

    private static void syncFind(final Client client, final String id) {
        final Optional<Device> result = client.sync().findById(id);
        System.out.format("find[sync]: %s -> %s%n", id,
                result
                        .map(Object::toString)
                        .orElse("<null>"));
    }

    private static void asyncSave(final Client client, final String id) throws InterruptedException {

        final Semaphore s = new Semaphore(0);

        final Device device = createNewDevice(id);

        final CompletionStage<String> f = client.async().save(device);

        f.whenComplete((result, error) -> {
            if (error != null) {
                System.err.println("save[async]: operation failed");
                error.printStackTrace();
            } else {
                System.out.format("save[async]: %s -> %s%n", device, result);
            }
        })
                .thenRun(s::release);

        if (!s.tryAcquire(5, TimeUnit.SECONDS)) {
            System.out.println("Cancel");
            f.toCompletableFuture().cancel(true);
        }
    }

    private static void asyncFind(final Client client, final String id) throws InterruptedException {
        final Semaphore s = new Semaphore(0);

        final CompletionStage<Optional<Device>> f = client.async().findById(id);

        f.whenComplete((result, error) -> {
            if (error != null) {
                System.err.println("find[async]: operation failed");
                error.printStackTrace();
            } else {
                System.out.format("find[async]: %s -> %s%n", id,
                        result
                                .map(Object::toString)
                                .orElse("<null>"));
            }
        })
                .thenRun(s::release);

        if (!s.tryAcquire(5, TimeUnit.SECONDS)) {
            System.out.println("Cancel");
            f.toCompletableFuture().cancel(true);
        }
    }

    private static Device createNewDevice(final String id) {
        final Instant now = Instant.now();
        final Device device = new Device(id, now, now, "my:device", new HashMap<>());
        return device;
    }
}
