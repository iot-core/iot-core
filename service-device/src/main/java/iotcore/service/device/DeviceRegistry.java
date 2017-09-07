package iotcore.service.device;

public interface DeviceRegistry {

    String save(Device device);

    Device findById(String deviceId);

}