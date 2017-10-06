package org.iotbricks.core.binding.proton;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.binding.amqp.AmqpRequestContext;
import org.iotbricks.core.binding.common.MessageResponseHandler;

/**
 * A specialized {@link MessageResponseHandler} for errors.
 * <p>
 * This handler also sets the application property {@code status} to {@code 500}
 * on the proton message. Indicating an error to the receiver.
 */
public class ProtonErrorMessageResponseHandler extends MessageResponseHandler<ProtonRequestContext, Message> {

    private static final Consumer<Message> SET_TO_500 = message -> {
        final Map<String, ?> properties = Collections.singletonMap("status", 500);
        message.setApplicationProperties(new ApplicationProperties(properties));
    };

    public ProtonErrorMessageResponseHandler(
            final Function<ProtonRequestContext, Optional<String>> responseAddressProvider) {
        super(responseAddressProvider, SET_TO_500);
    }

    public ProtonErrorMessageResponseHandler() {
        super(AmqpRequestContext::getReplyToAddress);
    }

}
