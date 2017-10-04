package iot.core.service.binding.amqp;

import static org.iotbricks.core.utils.binding.amqp.DefaultAmqpErrorConditionTranslator.instance;

import java.util.Objects;

import org.iotbricks.core.utils.binding.ErrorCondition;
import org.iotbricks.core.utils.binding.ErrorResult;
import org.iotbricks.core.utils.binding.amqp.AmqpErrorConditionTranslator;

import iot.core.service.binding.RequestContext;
import iot.core.service.binding.ResponseHandler;

/**
 * Handle error as AMQP delivery rejection.
 */
public class AmqpRejectResponseHandler implements ResponseHandler<ErrorResult, RequestContext, AmqpResponseContext> {

    private AmqpErrorConditionTranslator errorConditionTranslator;

    public AmqpRejectResponseHandler() {
        this(instance());
    }

    public AmqpRejectResponseHandler(final AmqpErrorConditionTranslator errorConditionTranslator) {
        Objects.requireNonNull(errorConditionTranslator);
        this.errorConditionTranslator = errorConditionTranslator;
    }

    @Override
    public void handle(final RequestContext request, final AmqpResponseContext response, final ErrorResult error) {
        response.reject(mapError(error.getCondition()), error.getMessage());
    }

    protected String mapError(final ErrorCondition errorCondition) {
        return errorConditionTranslator.toAmqp(errorCondition);
    }
}
