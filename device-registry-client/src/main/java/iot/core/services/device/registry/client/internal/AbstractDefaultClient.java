package iot.core.services.device.registry.client.internal;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import iot.core.services.device.registry.client.Client;
import iot.core.services.device.registry.client.DeviceRegistryAsync;
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
            public CompletionStage<String> save(final Device device) {
                return internalSave(device);
            }

            @Override
            public CompletionStage<Optional<Device>> findById(final String id) {
                return internalFindById(id);
            }
        };
    }

    protected abstract CompletionStage<Optional<Device>> internalFindById(final String id);

    protected abstract CompletionStage<String> internalSave(final Device device);

}
