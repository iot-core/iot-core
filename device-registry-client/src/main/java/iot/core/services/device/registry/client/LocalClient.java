package iot.core.services.device.registry.client;

import static iot.core.services.device.registry.client.util.CloseableCompletionStage.of;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import iot.core.services.device.registry.client.internal.AbstractDefaultClient;
import iot.core.services.device.registry.client.util.CloseableCompletionStage;
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
    protected CloseableCompletionStage<Optional<Device>> internalFindById(final String id) {
        return of(supplyAsync(() -> this.registry.findById(id), this.executionService));
    }

    @Override
    protected CloseableCompletionStage<String> internalSave(final Device device) {
        return of(supplyAsync(() -> this.registry.save(device)));
    }

    @Override
    protected CloseableCompletionStage<String> internalCreate(final Device device) {
        return of(supplyAsync(() -> this.registry.create(device)));
    }

    @Override
    protected CloseableCompletionStage<Void> internalUpdate(final Device device) {
        return of(runAsync(() -> this.registry.update(device)));
    }

}
