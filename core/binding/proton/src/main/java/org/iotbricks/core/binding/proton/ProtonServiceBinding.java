package org.iotbricks.core.binding.proton;

import org.iotbricks.core.binding.RequestHandler;

public class ProtonServiceBinding {
    private final String serviceName;
    private final RequestHandler<ProtonRequestContext> handler;

    public ProtonServiceBinding(final String serviceName, final RequestHandler<ProtonRequestContext> handler) {
        this.serviceName = serviceName;
        this.handler = handler;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public RequestHandler<ProtonRequestContext> getHandler() {
        return this.handler;
    }

}
