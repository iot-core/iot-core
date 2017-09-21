package iot.core.services.device.registry.client;

import iot.core.service.device.DeviceRegistry;

public interface Client extends AutoCloseable {

    DeviceRegistry sync();

    DeviceRegistryAsync async();
}
