package org.iotbricks.core.binding.proton;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.binding.ResponseHandler;
import org.iotbricks.core.binding.ServiceBinding;
import org.iotbricks.core.binding.common.DefaultErrorTranslator;
import org.iotbricks.core.proton.vertx.AbstractProtonConnection;
import org.iotbricks.core.proton.vertx.serializer.AmqpByteSerializer;
import org.iotbricks.core.proton.vertx.serializer.AmqpSerializer;
import org.iotbricks.core.utils.address.AddressProvider;
import org.iotbricks.core.utils.address.DefaultAddressProvider;
import org.iotbricks.core.utils.binding.ErrorResult;
import org.iotbricks.core.utils.binding.ErrorTranslator;
import org.iotbricks.core.utils.serializer.ByteSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonReceiver;
import io.vertx.proton.ProtonSender;

public class ProtonBindingServer extends AbstractProtonConnection {

    public final static class Builder extends AbstractProtonConnection.Builder<ProtonBindingServer, Builder> {

        private static final AddressProvider DEFAULT_ADDRESS_PROVIDER = DefaultAddressProvider.instance();

        private static final ErrorTranslator DEFAULT_ERROR_TRANSLATOR = new DefaultErrorTranslator();

        private AddressProvider addressProvider = DEFAULT_ADDRESS_PROVIDER;

        private ErrorTranslator errorTranslator = DEFAULT_ERROR_TRANSLATOR;

        private ResponseHandler<? super Object, ? super ProtonRequestContext, ? super ProtonResponseContext, Message> successHandler;

        private ResponseHandler<? super ErrorResult, ? super ProtonRequestContext, ? super ProtonResponseContext, Message> errorHandler;

        private AmqpSerializer serializer;

        private final List<ServiceBinding> bindings;

        private Builder() {
            this.bindings = new LinkedList<>();
        }

        private Builder(final Builder other) {
            super(other);
            this.addressProvider = other.addressProvider;
            this.serializer = other.serializer;
            this.errorTranslator = other.errorTranslator;
            this.successHandler = other.successHandler;
            this.errorHandler = other.errorHandler;
            this.bindings = new LinkedList<>(other.bindings);
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder addressProvider(final AddressProvider addressProvider) {
            this.addressProvider = addressProvider != null ? addressProvider : DEFAULT_ADDRESS_PROVIDER;
            return this;
        }

        public AddressProvider addressProvider() {
            return this.addressProvider;
        }

        public Builder serializer(final AmqpSerializer serializer) {
            Objects.requireNonNull(serializer);
            this.serializer = serializer;
            return this;
        }

        public Builder serializer(final ByteSerializer serializer) {
            Objects.requireNonNull(serializer);
            this.serializer = AmqpByteSerializer.of(serializer);
            return this;
        }

        public AmqpSerializer serializer() {
            return this.serializer;
        }

        public Builder errorTranslator(final ErrorTranslator errorTranslator) {
            this.errorTranslator = errorTranslator != null ? errorTranslator : DEFAULT_ERROR_TRANSLATOR;
            return this;
        }

        public ErrorTranslator errorTranslator() {
            return this.errorTranslator;
        }

        public Builder successHandler(
                final ResponseHandler<? super Object, ? super ProtonRequestContext, ? super ProtonResponseContext, Message> successHandler) {
            this.successHandler = successHandler;
            return this;
        }

        public ResponseHandler<? super Object, ? super ProtonRequestContext, ? super ProtonResponseContext, Message> successHandler() {
            return this.successHandler;
        }

        public Builder errorHandler(
                final ResponseHandler<? super ErrorResult, ? super ProtonRequestContext, ? super ProtonResponseContext, Message> errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public ResponseHandler<? super ErrorResult, ? super ProtonRequestContext, ? super ProtonResponseContext, Message> errorHandler() {
            return this.errorHandler;
        }

        public Builder binding(final ServiceBinding binding) {
            Objects.requireNonNull(binding);
            this.bindings.add(binding);
            return this;
        }

        public Builder bindings(final Iterable<ServiceBinding> bindings) {
            Objects.requireNonNull(bindings);
            bindings.forEach(this.bindings::add);
            return this;
        }

        @Override
        public ProtonBindingServer build(final Vertx vertx) {
            Objects.requireNonNull(this.serializer, "'serializer' must be set");
            Objects.requireNonNull(this.successHandler, "'successHandler' must be set");
            Objects.requireNonNull(this.errorHandler, "'errorHandler' must be set");
            return new ProtonBindingServer(vertx, new Builder(this));
        }

    }

    public static Builder newServer() {
        return new Builder();
    }

    public static Builder newServer(final Builder other) {
        return new Builder(other);
    }

    private static final Logger logger = LoggerFactory.getLogger(ProtonBindingServer.class);

    private final Builder options;

    public ProtonBindingServer(final Vertx vertx, final Builder options) {
        super(vertx, options);
        this.options = options;

        open();
    }

    @Override
    protected void performEstablished(final ProtonConnection connection) {
        super.performEstablished(connection);

        final ProtonSender sender = this.connection.createSender(null);

        sender.openHandler(senderReady -> {
            if (senderReady.failed()) {
                logger.warn("Failed to open sender", senderReady.cause());
                this.connection.close();
                return;
            }

            bindServicesToConnection(sender);
        });

        sender.open();
    }

    protected Future<?> bindServicesToConnection(final ProtonSender sender) {

        @SuppressWarnings("rawtypes")
        final List<Future> futures = new LinkedList<>();

        for (final ServiceBinding binding : this.options.bindings) {
            futures.add(bindServiceToConnection(binding, sender));
        }

        return CompositeFuture.all(futures);

    }

    protected Future<?> bindServiceToConnection(final ServiceBinding binding, final ProtonSender sender) {

        final String address = this.options.addressProvider().requestAddress(binding.getServiceName());

        final ProtonRequestProcessor processor = new ProtonRequestProcessor(
                this.options.serializer(),
                sender,
                this.options.successHandler(),
                this.options.errorHandler(),
                this.options.errorTranslator(),
                binding.getHandler());

        final Future<ProtonReceiver> result = Future.future();

        this.connection
                .createReceiver(address)
                .handler(processor.messageHandler())
                .openHandler(result.completer())
                .open();

        return result;
    }

}
