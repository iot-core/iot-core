package iotcore.service.device;

import java.util.HashMap;
import java.util.Map;

import static java.util.UUID.randomUUID;

public class InMemoryDeviceRegistry implements DeviceRegistry {

    private final Map<String, Device> devices = new HashMap<>();

    @Override public String save(Device device) {
        if(device.getDeviceId() == null) {
            device.setDeviceId(randomUUID().toString());
        }
        devices.put(device.getDeviceId(), device);
        return device.getDeviceId();
    }

    @Override public Device findById(String deviceId) {
        return devices.get(deviceId);
    }

}
