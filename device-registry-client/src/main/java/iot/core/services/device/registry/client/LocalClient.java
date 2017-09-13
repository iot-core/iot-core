package iot.core.services.device.registry.client;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import iot.core.services.device.registry.client.internal.AbstractDefaultClient;
import iotcore.service.device.Device;
import iotcore.service.device.DeviceRegistry;

public class LocalClient extends AbstractDefaultClient {

    private final DeviceRegistry registry;
    private final ExecutorService executionService;

    public LocalClient(final DeviceRegistry registry) {
        super(0, null);

        Objects.requireNonNull(registry);

        this.registry = registry;
        this.executionService = Executors.newCachedThreadPool();
    }

    @Override
    public void close() throws Exception {
        this.executionService.shutdown();
    }

    @Override
    protected CompletionStage<Optional<Device>> internalFindById(final String id) {
        return supplyAsync(() -> this.registry.findById(id), this.executionService);
    }

    @Override
    protected CompletionStage<String> internalSave(final Device device) {
        return supplyAsync(() -> this.registry.save(device));
    }

}
