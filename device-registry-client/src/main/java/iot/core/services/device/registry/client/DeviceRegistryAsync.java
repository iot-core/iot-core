package iot.core.services.device.registry.client;

import java.util.Optional;

import io.glutamate.util.concurrent.CloseableCompletionStage;
import iotcore.service.device.Device;

public interface DeviceRegistryAsync {
    public CloseableCompletionStage<String> save(Device device);

    public CloseableCompletionStage<Optional<Device>> findById(String id);

    public CloseableCompletionStage<String> create(Device device);

    public CloseableCompletionStage<Void> update(Device device);

}
