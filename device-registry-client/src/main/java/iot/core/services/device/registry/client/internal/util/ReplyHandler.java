package iot.core.services.device.registry.client.internal.util;

@FunctionalInterface
public interface ReplyHandler<R, M> {
    public R handleReply(M reply) throws Exception;
}