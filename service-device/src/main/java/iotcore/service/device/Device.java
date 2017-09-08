package iotcore.service.device;

import java.util.Map;

public class Device {

    private String deviceId;

    private Map<String, Object> properties;

    public Device() {
    }

    public Device(String deviceId, Map<String, Object> properties) {
        this.deviceId = deviceId;
        this.properties = properties;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}