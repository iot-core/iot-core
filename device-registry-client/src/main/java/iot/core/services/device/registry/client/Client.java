package iot.core.services.device.registry.client;

import java.time.Duration;

import org.iotbricks.service.device.registry.api.DeviceRegistryService;

public interface Client extends AutoCloseable {

    DeviceRegistryService sync();

    DeviceRegistryService sync(Duration timeout);

    DeviceRegistryAsync async();

    @Override
    public default void close() throws Exception {
    }

}
