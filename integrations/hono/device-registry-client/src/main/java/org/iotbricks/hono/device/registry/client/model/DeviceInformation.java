package org.iotbricks.hono.device.registry.client.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class DeviceInformation {
    @JsonProperty("device-id")
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", this.id)
                .add("data", this.data)
                .toString();
    }

}
