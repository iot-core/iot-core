package org.iotbricks.service.device.registry.api;

import java.util.Optional;

import org.iotbricks.annotations.ServiceName;

/**
 * Service providing access to a basic list of devices and their state.
 */
@ServiceName("device")
public interface DeviceRegistryService {

    String create(Device device);

    void update(Device device);

    String save(Device device);

    Optional<Device> findById(String deviceId);

}