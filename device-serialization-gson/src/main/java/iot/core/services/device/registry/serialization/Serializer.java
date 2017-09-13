package iot.core.services.device.registry.serialization;

import iotcore.service.device.Device;

public interface Serializer {
    public String encode(Device device);

    public Device decodeDevice(String value);
}
