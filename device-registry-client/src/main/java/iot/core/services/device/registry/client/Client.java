package iot.core.services.device.registry.client;

import org.iotbricks.service.device.registry.api.DeviceRegistryService;

import java.time.Duration;

public interface Client extends AutoCloseable {

    DeviceRegistryService sync();

    DeviceRegistryService sync(Duration timeout);

    DeviceRegistryAsync async();
}
