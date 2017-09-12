package iotcore.service.device;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.UUID.randomUUID;

public class InMemoryDeviceRegistry implements DeviceRegistry {

    private final Map<String, Device> devices = new HashMap<>();

    @Override public String create(Device device) {
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
        if(!devices.containsKey(device.getDeviceId())) {
            throw new IllegalArgumentException("Device with given ID does not exist.");
        }

        devices.put(device.getDeviceId(), device);
    }

    @Override public String save(Device device) {
        if(devices.containsKey(device.getDeviceId())) {
            update(device);
            return device.getDeviceId();
        } else {
            return create(device);
        }
    }

    @Override public Optional<Device> findById(String deviceId) {
        return Optional.ofNullable(devices.get(deviceId));
    }

}
