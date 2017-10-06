package org.iotbricks.core.amqp.transport.internal;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;

public final class Properties {
    private Properties() {
    }

    /**
     * Extract the {@code status} value as Integer from the message
     *
     * @param message
     *            the message to extract the value from
     * @return the status value, never {@code null}, but may be
     *         {@link Optional#empty()}
     */
    public static Optional<Integer> status(final Message message) {
        Objects.requireNonNull(message);

        // get application properties

        final ApplicationProperties properties = message.getApplicationProperties();
        if (properties == null) {
            return empty();
        }

        // get value map

        final Map<?, ?> value = properties.getValue();
        if (value == null) {
            return empty();
        }

        // get status value

        final Object status = value.get("status");
        if (status instanceof Number) {
            // return value as int
            return of(((Number) status).intValue());
        }

        // return nothing

        return empty();
    }
}
