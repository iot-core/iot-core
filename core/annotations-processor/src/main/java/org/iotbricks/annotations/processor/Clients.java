package org.iotbricks.annotations.processor;

import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

import org.iotbricks.annotations.Client;

public final class Clients {

    private Clients() {
    }

    public static TypeMirror getServiceType(final Client client) {
        try {
            client.value();
            throw new IllegalStateException("Unable to get service type");
        } catch (final MirroredTypeException e) {
            return e.getTypeMirror();
        }
    }
}
