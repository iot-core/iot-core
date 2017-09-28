package iot.core.service.binding;

@FunctionalInterface
public interface RequestHandler<C extends RequestContext> {
    public Object process(C context) throws Exception;
}