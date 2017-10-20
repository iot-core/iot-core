package org.iotbricks.core.amqp.transport.proton;

import static java.util.UUID.randomUUID;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.iotbricks.core.amqp.transport.RequestInstance;

import io.vertx.core.Future;
import io.vertx.proton.ProtonConnection;

/**
 * An abstract request sender which uses a set of shared response addresses,
 * derived from the request and the current connection.
 * <p>
 * When a new connection is created a new internal token will be generated using
 * the token supplier. This token will be stored and can be used to create the
 * response address based on the combination of the request and the token.
 *
 * @param <RQ>
 *            The type of the request
 */
public class SharedClientAndRequestReceiverRequestSender<T, RQ extends Request>
        extends AbstractSharedRequestReceiverRequestSender<RQ> {

    private final Supplier<T> tokenSupplier;
    private final BiFunction<T, RQ, String> responseAddressProvider;
    private T token;

    private SharedClientAndRequestReceiverRequestSender(final Supplier<T> tokenSupplier,
            final BiFunction<T, RQ, String> responseAddressProvider) {
        this.tokenSupplier = tokenSupplier;
        this.responseAddressProvider = responseAddressProvider;

    }

    @Override
    public Future<?> connected(final ProtonConnection connection) {
        this.token = this.tokenSupplier.get();
        return super.connected(connection);
    }

    @Override
    protected String responseAddress(final RequestInstance<?, RQ> request) {
        return this.responseAddressProvider.apply(this.token, request.getRequest());
    }

    public static <T, RQ extends Request> RequestSender<RQ> of(final Supplier<T> tokenSupplier,
            final BiFunction<T, RQ, String> responseAddressProvider) {

        Objects.requireNonNull(tokenSupplier);
        Objects.requireNonNull(responseAddressProvider);

        return new SharedClientAndRequestReceiverRequestSender<>(tokenSupplier, responseAddressProvider);
    }

    public static <RQ extends Request> RequestSender<RQ> uuid(
            final BiFunction<UUID, RQ, String> responseAddressProvider) {

        Objects.requireNonNull(responseAddressProvider);

        return new SharedClientAndRequestReceiverRequestSender<>(() -> randomUUID(), responseAddressProvider);
    }

    public static <RQ extends Request> Supplier<RequestSender<RQ>> uuidFactory(
            final BiFunction<UUID, RQ, String> responseAddressProvider) {

        Objects.requireNonNull(responseAddressProvider);

        return () -> new SharedClientAndRequestReceiverRequestSender<>(() -> randomUUID(), responseAddressProvider);
    }

}
