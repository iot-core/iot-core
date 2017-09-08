package iot.core.services.device.registry.client.internal;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import iot.core.services.device.registry.client.DeviceRegistryAsync;
import iotcore.service.device.Device;
import iotcore.service.device.DeviceRegistry;

public class SyncDeviceRegistryWrapper implements DeviceRegistry {
    private final DeviceRegistryAsync async;

    public SyncDeviceRegistryWrapper(final DeviceRegistryAsync async) {
        this.async = async;
    }

    private static <T> T await(final CompletionStage<T> stage) {
        try {
            return stage.toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String save(final Device device) {
        return await(this.async.save(device));
    }

    @Override
    public Device findById(final String deviceId) {
        return await(this.async.findById(deviceId)).orElse(null);
    }

}
