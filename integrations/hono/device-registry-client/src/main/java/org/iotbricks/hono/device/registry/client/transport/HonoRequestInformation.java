package org.iotbricks.hono.device.registry.client.transport;

public class HonoRequestInformation {

    private final String service;
    private final String tenant;

    public HonoRequestInformation(final String service, final String tenant) {
        this.service = service;
        this.tenant = tenant;
    }

    public String getService() {
        return this.service;
    }

    public String getTenant() {
        return this.tenant;
    }

}
