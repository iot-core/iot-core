package org.iotbricks.core.amqp.transport.proton;

import java.util.Objects;
import java.util.function.Function;

import org.iotbricks.core.amqp.transport.RequestInstance;

/**
 * An abstract request sender which uses a set of shared response addresses,
 * solely derived from the request.
 *
 * @param <RQ>
 *            The type of the request
 */
public class SharedRequestReceiverRequestSender<RQ extends Request>
        extends AbstractSharedRequestReceiverRequestSender<RQ> {

    private final Function<RQ, String> responseAddressProvider;

    private SharedRequestReceiverRequestSender(final Function<RQ, String> responseAddressProvider) {
        this.responseAddressProvider = responseAddressProvider;
    }

    @Override
    protected String responseAddress(final RequestInstance<?, RQ> request) {
        return this.responseAddressProvider.apply(request.getRequest());
    }

    public static <RQ extends Request> RequestSender<RQ> of(final Function<RQ, String> responseAddressProvider) {
        Objects.requireNonNull(responseAddressProvider);

        return new SharedRequestReceiverRequestSender<>(responseAddressProvider);
    }
}