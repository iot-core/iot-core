package iot.core.services.device.registry.client.internal;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import iot.core.services.device.registry.client.Client;
import iot.core.services.device.registry.client.DeviceRegistryAsync;
import iot.core.services.device.registry.client.util.CloseableCompletionStage;
import iotcore.service.device.Device;
import iotcore.service.device.DeviceRegistry;

public abstract class AbstractDefaultClient implements Client {

    private final long timeout;
    private final TimeUnit timeUnit;

    public AbstractDefaultClient(final long timeout, final TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public DeviceRegistry sync() {
        return new SyncDeviceRegistryWrapper(async(), this.timeout, this.timeUnit);
    }

    @Override
    public DeviceRegistryAsync async() {
        return new DeviceRegistryAsync() {

            @Override
            public CloseableCompletionStage<String> save(final Device device) {
                return internalSave(device);
            }

            @Override
            public CloseableCompletionStage<Optional<Device>> findById(final String id) {
                return internalFindById(id);
            }

            @Override
            public CloseableCompletionStage<String> create(final Device device) {
                return internalCreate(device);
            }

            @Override
            public CloseableCompletionStage<Void> update(final Device device) {
                return internalUpdate(device);
            }
        };
    }

    protected abstract CloseableCompletionStage<Optional<Device>> internalFindById(String id);

    protected abstract CloseableCompletionStage<String> internalSave(Device device);

    protected abstract CloseableCompletionStage<Void> internalUpdate(Device device);

    protected abstract CloseableCompletionStage<String> internalCreate(Device device);

}
