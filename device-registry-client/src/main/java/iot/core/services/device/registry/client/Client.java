package iot.core.services.device.registry.client;

import java.time.Duration;

import iot.core.service.device.DeviceRegistry;

public interface Client extends AutoCloseable {

    DeviceRegistry sync();

    DeviceRegistry sync(Duration timeout);

    DeviceRegistryAsync async();
}
