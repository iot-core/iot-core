package iot.core.services.device.registry.client.internal;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import iot.core.services.device.registry.client.DeviceRegistryAsync;
import iot.core.services.device.registry.client.util.Await;
import iot.core.services.device.registry.client.util.CloseableCompletionStage;
import iotcore.service.device.Device;
import iotcore.service.device.DeviceRegistry;

public class SyncDeviceRegistryWrapper implements DeviceRegistry {
    private final DeviceRegistryAsync async;
    private final long timeout;
    private final TimeUnit timeUnit;

    public SyncDeviceRegistryWrapper(final DeviceRegistryAsync async, final long timeout, final TimeUnit timeUnit) {
        this.async = async;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    private <T> T await(final CloseableCompletionStage<T> stage) {
        try {
            return Await.await(stage, this.timeout, this.timeUnit);
        } finally {
            try {
                stage.close();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String create(final Device device) {
        return await(this.async.create(device));
    }

    @Override
    public void update(final Device device) {
        await(this.async.update(device));
    }

    @Override
    public String save(final Device device) {
        return await(this.async.save(device));
    }

    @Override
    public Optional<Device> findById(final String deviceId) {
        return await(this.async.findById(deviceId));
    }

}
