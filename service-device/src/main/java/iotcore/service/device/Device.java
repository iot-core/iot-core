package iotcore.service.device;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;
import java.util.Map;

public class Device {

    private String deviceId;

    private Date created;

    private Date updated;

    private String type;

    private Map<String, Object> properties;

    public Device() {
    }

    public Device(String deviceId, Date created, Date updated, String type, Map<String, Object> properties) {
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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
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