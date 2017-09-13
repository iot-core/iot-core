package iot.core.services.device.registry.client.internal;

import static iot.core.services.device.registry.client.Await.await;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import iot.core.services.device.registry.client.DeviceRegistryAsync;
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

    @Override
    public String create(final Device device) {
        return null;
    }

    @Override
    public void update(final Device device) {

    }

    @Override
    public String save(final Device device) {
        return await(this.async.save(device), this.timeout, this.timeUnit);
    }

    @Override
    public Optional<Device> findById(final String deviceId) {
        return await(this.async.findById(deviceId), this.timeout, this.timeUnit);
    }

}
