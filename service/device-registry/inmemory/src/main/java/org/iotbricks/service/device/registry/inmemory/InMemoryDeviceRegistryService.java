package org.iotbricks.service.device.registry.inmemory;

import org.iotbricks.service.device.registry.api.Device;
import org.iotbricks.service.device.registry.api.DeviceRegistryService;
import org.iotbricks.service.device.registry.spi.DeviceSchemaValidator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.UUID.randomUUID;

public class InMemoryDeviceRegistryService implements DeviceRegistryService {

    private final DeviceSchemaValidator schemaValidator;

    private final Map<String, Device> devices = new HashMap<>();

    public InMemoryDeviceRegistryService(DeviceSchemaValidator schemaValidator) {
        this.schemaValidator = schemaValidator;
    }

    @Override public String create(Device device) {
        schemaValidator.validate(device);

        if(devices.containsKey(device.getDeviceId())) {
            throw new IllegalArgumentException("Device with given ID already exists.");
        }

        if(device.getDeviceId() == null) {
            device.setDeviceId(randomUUID().toString());
        }
        devices.put(device.getDeviceId(), device);
        return device.getDeviceId();
    }

    @Override public void update(Device device) {
        schemaValidator.validate(device);

        if(!devices.containsKey(device.getDeviceId())) {
            throw new IllegalArgumentException("Device with given ID does not exist.");
        }

        devices.put(device.getDeviceId(), device);
    }

    @Override public String save(Device device) {
        schemaValidator.validate(device);

        if(devices.containsKey(device.getDeviceId())) {
            update(device);
            return device.getDeviceId();
        } else {
            return create(device);
        }
    }

    @Override public void remove(String deviceId) {
        devices.remove(deviceId);
    }

    @Override public Optional<Device> findById(String deviceId) {
        return Optional.ofNullable(devices.get(deviceId));
    }

}
