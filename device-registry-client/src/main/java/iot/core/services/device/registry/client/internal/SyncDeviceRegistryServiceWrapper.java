package iot.core.services.device.registry.client.internal;

import java.time.Duration;
import java.util.Optional;

import iot.core.services.device.registry.client.DeviceRegistryServiceAsync;
import iot.core.utils.client.AbstractSyncWrapper;
import org.iotbricks.service.device.registry.api.Device;
import org.iotbricks.service.device.registry.api.DeviceRegistryService;

public class SyncDeviceRegistryServiceWrapper extends AbstractSyncWrapper implements DeviceRegistryService {
    private final DeviceRegistryServiceAsync async;

    public SyncDeviceRegistryServiceWrapper(final DeviceRegistryServiceAsync async, final Duration timeout) {
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
