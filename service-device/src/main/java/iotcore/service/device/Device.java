package iotcore.service.device;

public class Device {

    private final String deviceId;

    public Device(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

}