package org.iotbricks.core.binding;

@FunctionalInterface
public interface RequestHandler<C extends RequestContext> {
    public Object process(C context) throws Exception;
}