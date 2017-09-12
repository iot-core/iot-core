package iotcore.service.device;

import java.util.Optional;

public interface DeviceRegistry {

    String create(Device device);

    void update(Device device);

    String save(Device device);

    Optional<Device> findById(String deviceId);

}