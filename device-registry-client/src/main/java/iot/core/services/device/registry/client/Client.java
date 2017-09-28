package iot.core.services.device.registry.client;

import org.iotbricks.service.device.registry.api.DeviceRegistry;

import java.time.Duration;

public interface Client extends AutoCloseable {

    DeviceRegistry sync();

    DeviceRegistry sync(Duration timeout);

    DeviceRegistryAsync async();
}
