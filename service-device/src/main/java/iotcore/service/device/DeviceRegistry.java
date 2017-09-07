package iotcore.service.device;

import java.util.Optional;

public interface DeviceRegistry {

    String save(Device device);

    Device findById(String deviceId);

}