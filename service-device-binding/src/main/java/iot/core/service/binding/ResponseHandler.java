package iot.core.service.binding;

public interface ResponseHandler<T, C1 extends RequestContext, C2 extends ResponseContext> {
    public void handle(C1 request, C2 response, T value);
}
