package org.iotbricks.core.amqp.transport.client;

import static java.util.Optional.ofNullable;
import static org.iotbricks.core.amqp.transport.utils.Properties.status;
import static org.iotbricks.core.utils.binding.ErrorCondition.INTERNAL_ERROR;

import java.util.Objects;
import java.util.Optional;

import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.message.Message;
import org.iotbricks.core.amqp.transport.RequestInstance;
import org.iotbricks.core.amqp.transport.ResponseHandler;
import org.iotbricks.core.amqp.transport.proton.ProtonTransport;
import org.iotbricks.core.proton.vertx.serializer.AmqpSerializer;
import org.iotbricks.core.utils.binding.ErrorResult;
import org.iotbricks.core.utils.binding.RequestException;
import org.iotbricks.core.utils.binding.amqp.AmqpErrorConditionTranslator;
import org.iotbricks.core.utils.binding.amqp.DefaultAmqpErrorConditionTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.glutamate.util.Optionals;
import io.glutamate.util.concurrent.CloseableCompletionStage;
import io.vertx.core.Vertx;

public class AmqpClientTransport extends ProtonTransport<ServiceRequestInformation>
        implements ClientTransport<Message> {

    private static final Logger logger = LoggerFactory.getLogger(AmqpClientTransport.class);

    public static class Builder
            extends ProtonTransport.Builder<ServiceRequestInformation, AmqpClientTransport, Builder> {

        private static final AmqpErrorConditionTranslator DEFAULT_ERROR_CONDITION_TRANSLATOR = DefaultAmqpErrorConditionTranslator
                .instance();
        private AmqpSerializer serializer;
        private AmqpErrorConditionTranslator errorConditionTranslator = DEFAULT_ERROR_CONDITION_TRANSLATOR;

        private Builder() {
        }

        private Builder(final Builder other) {
            super(other);

            this.serializer = other.serializer;
            this.errorConditionTranslator = other.errorConditionTranslator;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        public Builder serializer(final AmqpSerializer serializer) {
            Objects.requireNonNull(serializer);
            this.serializer = serializer;
            return this;
        }

        public AmqpSerializer serializer() {
            return this.serializer;
        }

        public Builder errorConditionTranslator(final AmqpErrorConditionTranslator errorConditionTranslator) {
            this.errorConditionTranslator = errorConditionTranslator != null ? errorConditionTranslator
                    : DEFAULT_ERROR_CONDITION_TRANSLATOR;
            return this;
        }

        public AmqpErrorConditionTranslator errorConditionTranslator() {
            return this.errorConditionTranslator;
        }

        @Override
        public void validate() {
            super.validate();
            Objects.requireNonNull(this.serializer, "'serializer' must be set");
        }

        @Override
        public AmqpClientTransport build(final Vertx vertx) {
            validate();
            return new AmqpClientTransport(vertx, new Builder(this));
        }
    }

    public static Builder newTransport() {
        return new Builder();
    }

    public static Builder newTransport(final Builder other) {
        Objects.requireNonNull(other);
        return new Builder(other);
    }

    private final Builder options;

    public AmqpClientTransport(final Vertx vertx, final Builder options) {
        super(vertx, ServiceRequestInformation::getService, options);

        logger.debug("Creating AMQP transport - {}", options);

        this.options = options;

        open();
    }

    @Override
    public <R> ServiceRequestBuilder<R> newRequest(final ResponseHandler<R, Message> responseHandler) {
        return new ServiceRequestBuilder<>(createRequestExecutor(responseHandler));
    }

    public <R> ServiceRequestBuilder<R> request(final String service, final String verb,
            final ResponseHandler<R, Message> responseHandler) {

        final ResponseHandler<R, Message> handler = response -> {

            if (Optionals.presentAndEqual(status(response), 500)) {
                // ERROR
                throw unwrapRemoteExceptionFromMessage(response);
            } else {
                return responseHandler.response(response);
            }

        };

        return newRequest(handler)
                .subject(verb)
                .rejected(this::handleRejected)
                .service(service);
    }

    @Override
    public <R> CloseableCompletionStage<R> request(final String service, final String verb, final Object[] requestBody,
            final ResponseHandler<R, Message> responseHandler) {

        return request(service, verb, responseHandler)
                .payload(this.options.serializer().encode(requestBody))
                .execute();

    }

    @Override
    public <R> CloseableCompletionStage<R> request(final String service, final String verb, final Object requestBody,
            final ResponseHandler<R, Message> responseHandler) {

        return request(service, verb, responseHandler)
                .payload(this.options.serializer().encode(requestBody))
                .execute();

    }

    @Override
    public ResponseHandler<Void, Message> ignoreBody() {
        return msg -> null;
    }

    @Override
    public <T> ResponseHandler<T, Message> bodyAs(final Class<T> clazz) {
        return msg -> this.options.serializer().decode(msg.getBody(), clazz);
    }

    @Override
    public <T> ResponseHandler<Optional<T>, Message> bodyAsOptional(final Class<T> clazz) {
        return msg -> ofNullable(this.options.serializer().decode(msg.getBody(), clazz));
    }

    protected void handleRejected(final Rejected rejected, final RequestInstance<?, ?> request) {
        request.fail(unwrapRemoteExceptionFromReject(rejected));
    }

    protected Exception unwrapRemoteExceptionFromMessage(final Message message) {

        final ErrorResult error = this.options.serializer().decode(message.getBody(), ErrorResult.class);

        org.iotbricks.core.utils.binding.ErrorCondition condition = error.getCondition();

        if (condition == null) {
            condition = INTERNAL_ERROR;
        }

        return new RequestException(condition, error.getMessage());
    }

    protected Exception unwrapRemoteExceptionFromReject(final Rejected state) {

        final ErrorCondition error = state.getError();

        if (error == null || error.getCondition() == null) {
            return new RuntimeException("Unknown remote exception");
        }

        final org.iotbricks.core.utils.binding.ErrorCondition condition = this.options.errorConditionTranslator()
                .fromAmqp(error.getCondition().toString());
        final String message = state.getError().getDescription();

        return new RequestException(condition, message);
    }

}