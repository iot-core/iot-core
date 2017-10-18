package org.iotbricks.core.amqp.transport.proton;

import org.apache.qpid.proton.message.Message;

public interface Request {
    public Message getMessage();
}
