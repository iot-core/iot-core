package org.iotbricks.core.binding.common;

import org.iotbricks.core.binding.RequestContext;
import org.iotbricks.core.binding.RequestHandler;
import org.iotbricks.core.binding.ServiceBinding;

public class DefaultServiceBinding implements ServiceBinding {
    private final String serviceName;
    private final RequestHandler<RequestContext> handler;

    public DefaultServiceBinding(final String serviceName, final RequestHandler<RequestContext> handler) {
        this.serviceName = serviceName;
        this.handler = handler;
    }

    /* (non-Javadoc)
     * @see org.iotbricks.core.binding.common.ServiceBinding#getServiceName()
     */
    @Override
    public String getServiceName() {
        return this.serviceName;
    }

    /* (non-Javadoc)
     * @see org.iotbricks.core.binding.common.ServiceBinding#getHandler()
     */
    @Override
    public RequestHandler<RequestContext> getHandler() {
        return this.handler;
    }

}
