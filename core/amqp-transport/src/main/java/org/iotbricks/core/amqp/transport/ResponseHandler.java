package org.iotbricks.core.amqp.transport;

@FunctionalInterface
public interface ResponseHandler<T, M> {
    public T response(M response) throws Exception;
}