package iot.core.services.device.registry.client;

import iotcore.service.device.DeviceRegistry;

public interface Client extends AutoCloseable {

    DeviceRegistry sync();

    DeviceRegistryAsync async();
}
