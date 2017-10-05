package org.iotbricks.core.binding.amqp;

import static org.iotbricks.core.utils.binding.amqp.DefaultAmqpErrorConditionTranslator.instance;

import java.util.Objects;

import org.iotbricks.core.binding.RequestContext;
import org.iotbricks.core.binding.ResponseHandler;
import org.iotbricks.core.utils.binding.ErrorCondition;
import org.iotbricks.core.utils.binding.ErrorResult;
import org.iotbricks.core.utils.binding.amqp.AmqpErrorConditionTranslator;

/**
 * Handle error as AMQP delivery rejection.
 */
public class AmqpRejectResponseHandler<M> implements ResponseHandler<ErrorResult, RequestContext, AmqpResponseContext<M>, M> {

    private AmqpErrorConditionTranslator errorConditionTranslator;

    public AmqpRejectResponseHandler() {
        this(instance());
    }

    public AmqpRejectResponseHandler(final AmqpErrorConditionTranslator errorConditionTranslator) {
        Objects.requireNonNull(errorConditionTranslator);
        this.errorConditionTranslator = errorConditionTranslator;
    }

    @Override
    public void handle(final RequestContext request, final AmqpResponseContext<M> response, final ErrorResult error) {
        response.reject(mapError(error.getCondition()), error.getMessage());
    }

    protected String mapError(final ErrorCondition errorCondition) {
        return errorConditionTranslator.toAmqp(errorCondition);
    }
}
