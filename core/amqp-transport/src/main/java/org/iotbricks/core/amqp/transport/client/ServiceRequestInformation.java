package org.iotbricks.core.amqp.transport.client;

public final class ServiceRequestInformation {
    private final String service;

    public ServiceRequestInformation(final String service) {
        this.service = service;
    }

    public String getService() {
        return this.service;
    }

}