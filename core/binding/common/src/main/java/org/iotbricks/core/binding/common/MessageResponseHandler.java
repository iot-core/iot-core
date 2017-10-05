package org.iotbricks.core.binding.common;

import static org.iotbricks.core.utils.binding.ErrorCondition.DECODE_ERROR;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.iotbricks.core.binding.RequestContext;
import org.iotbricks.core.binding.ResponseContext;
import org.iotbricks.core.binding.ResponseHandler;
import org.iotbricks.core.utils.binding.RequestException;

public class MessageResponseHandler<C extends RequestContext, M>
        implements ResponseHandler<Object, C, ResponseContext<M>, M> {

    private final Function<C, Optional<String>> responseAddressProvider;
    private final Consumer<M> mesageCustomizer;

    public MessageResponseHandler(final Function<C, Optional<String>> responseAddressProvider) {
        this(responseAddressProvider, message -> {
        });
    }

    public MessageResponseHandler(final Function<C, Optional<String>> responseAddressProvider,
            final Consumer<M> mesageCustomizer) {
        Objects.requireNonNull(responseAddressProvider);

        this.responseAddressProvider = responseAddressProvider;
        this.mesageCustomizer = mesageCustomizer;
    }

    @Override
    public void handle(final C request, final ResponseContext<M> response, final Object value) {

        /*
         * TODO: Investigate if it would be better to do a pre-flight check for the
         * reply address and fail before actually processing the request.
         */

        final Optional<String> address = this.responseAddressProvider.apply(request);
        response.sendMessage(
                address.orElseThrow(() -> new RequestException(DECODE_ERROR, "No replyTo address")),
                value,
                this.mesageCustomizer);
    }

}
