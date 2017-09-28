package iot.core.services.device.registry.client.internal;

import java.time.Duration;
import java.util.Optional;

import io.glutamate.util.concurrent.CloseableCompletionStage;
import iot.core.services.device.registry.client.Client;
import iot.core.services.device.registry.client.DeviceRegistryAsync;
import org.iotbricks.service.device.registry.api.Device;
import org.iotbricks.service.device.registry.api.DeviceRegistry;

public abstract class AbstractDefaultClient implements Client {

    private final Duration timeout;

    public AbstractDefaultClient(final Duration timeout) {
        this.timeout = timeout;
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public DeviceRegistry sync() {
        return new SyncDeviceRegistryWrapper(async(), this.timeout);
    }

    @Override
    public DeviceRegistry sync(final Duration timeout) {
        if (timeout == null) {
            return sync();
        }
        return new SyncDeviceRegistryWrapper(async(), timeout.abs());
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
