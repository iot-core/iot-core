package org.iotbricks.hono.device.registry.client.model;

import java.util.Map;

public class DeviceInformation {
    private String id;
    private Map<String, ?> data;

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Map<String, ?> getData() {
        return this.data;
    }

    public void setData(final Map<String, ?> data) {
        this.data = data;
    }

}
