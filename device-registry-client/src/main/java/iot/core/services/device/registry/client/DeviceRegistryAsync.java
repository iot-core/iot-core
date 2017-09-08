package iot.core.services.device.registry.client;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import iotcore.service.device.Device;

public interface DeviceRegistryAsync {
    public CompletionStage<String> save(Device device);
    public CompletionStage<Optional<Device>> findById ( String id );
}
