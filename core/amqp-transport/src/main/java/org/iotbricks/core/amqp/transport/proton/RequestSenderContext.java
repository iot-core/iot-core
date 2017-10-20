package org.iotbricks.core.amqp.transport.proton;

import org.iotbricks.core.amqp.transport.internal.Buffer;

public interface RequestSenderContext<RQ extends Request> {

    public Buffer<RQ> getBuffer();

    public void senderReady(String address);
}