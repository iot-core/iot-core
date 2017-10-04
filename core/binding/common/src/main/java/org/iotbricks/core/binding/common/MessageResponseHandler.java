package org.iotbricks.core.binding.common;

import static org.iotbricks.core.utils.binding.ErrorCondition.DECODE_ERROR;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.iotbricks.core.binding.RequestContext;
import org.iotbricks.core.binding.ResponseContext;
import org.iotbricks.core.binding.ResponseHandler;
import org.iotbricks.core.utils.binding.RequestException;

public class MessageResponseHandler<C extends RequestContext> implements ResponseHandler<Object, C, ResponseContext> {

    private final Function<C, Optional<String>> responseAddressProvider;

    public MessageResponseHandler(final Function<C, Optional<String>> responseAddressProvider) {
        Objects.requireNonNull(responseAddressProvider);

        this.responseAddressProvider = responseAddressProvider;
    }

    @Override
    public void handle(final C request, final ResponseContext response, final Object value) {

        /*
         * TODO: Investigate if it would be better to do a pre-flight check for the
         * reply address and fail before actually processing the request.
         */

        final Optional<String> address = responseAddressProvider.apply(request);
        response.sendMessage(
                address.orElseThrow(() -> new RequestException(DECODE_ERROR, "No replyTo address")),
                value);
    }

}
