package iot.core.amqp.transport;

@FunctionalInterface
public interface ReplyHandler<R, M> {
    public R handleReply(M reply) throws Exception;
}