package iot.core.services.device.registry.client.internal;

import java.time.Duration;
import java.util.Optional;

import iot.core.services.device.registry.client.DeviceRegistryAsync;
import iot.core.utils.client.AbstractSyncWrapper;
import org.iotbricks.service.device.registry.api.Device;
import org.iotbricks.service.device.registry.api.DeviceRegistry;

public class SyncDeviceRegistryWrapper extends AbstractSyncWrapper implements DeviceRegistry {
    private final DeviceRegistryAsync async;

    public SyncDeviceRegistryWrapper(final DeviceRegistryAsync async, final Duration timeout) {
        super(timeout);
        this.async = async;
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
