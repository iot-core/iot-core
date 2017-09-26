package iot.core.service.device;

import java.time.Instant;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Device {

    private String deviceId;

    private Instant created;

    private Instant updated;

    private String type;

    private Map<String, Object> properties;

    public Device() {
    }

    public Device(String deviceId, Instant created, Instant updated, String type, Map<String, Object> properties) {
        this.deviceId = deviceId;
        this.created = created;
        this.updated = updated;
        this.type = type;
        this.properties = properties;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Instant getUpdated() {
        return updated;
    }

    public void setUpdated(Instant updated) {
        this.updated = updated;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}