package org.iotbricks.core.binding;

public interface RequestProcessor<C1 extends RequestContext, C2 extends ResponseContext> {
    public void process(C1 requestContext, C2 responseContext);
}
