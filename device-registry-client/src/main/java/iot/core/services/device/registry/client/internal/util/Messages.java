package iot.core.services.device.registry.client.internal.util;

import java.nio.ByteBuffer;

import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.message.Message;

public final class Messages {
    private Messages() {
    }

    public static ByteBuffer bodyAsBlob(final Message msg) {
        return ((Data) msg.getBody()).getValue().asByteBuffer();
    }
}
