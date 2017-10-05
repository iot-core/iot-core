package org.iotbricks.core.binding;

public interface ServiceBinding {

    public String getServiceName();

    public RequestHandler<RequestContext> getHandler();

}