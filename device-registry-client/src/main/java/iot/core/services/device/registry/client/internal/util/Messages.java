package iot.core.services.device.registry.client.internal.util;

import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.message.Message;

public final class Messages {
    private Messages() {
    }


    public static String bodyAsString(final Message msg) {
        return (String) ((AmqpValue)msg.getBody()).getValue();
    }

}
