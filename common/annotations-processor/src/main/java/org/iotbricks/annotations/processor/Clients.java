package org.iotbricks.annotations.processor;

import javax.lang.model.type.MirroredTypeException;

import org.iotbricks.annotations.Client;

public final class Clients {

    private Clients() {
    }

    public static String getServiceType(final Client client) {
        try {
            client.value();
            throw new IllegalStateException("Unable to get service type");
        } catch (final MirroredTypeException e) {
            return e.getTypeMirror().toString();
        }
    }
}
