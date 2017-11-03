package org.iotbricks.service.device.registry.api;

import java.util.Optional;

import org.iotbricks.annotations.ServiceName;

/**
 * Service providing access to a basic list of devices and their state.
 */
@ServiceName("device")
public interface DeviceRegistryService {

    String save(Device device);

    void remove(String deviceId);

    Optional<Device> findById(String deviceId);

}