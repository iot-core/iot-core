package iot.core.utils.binding.amqp;

import iot.core.utils.binding.ErrorCondition;

public interface AmqpErrorConditionTranslator {
    public ErrorCondition fromAmqp(String errorCondition);
    public String toAmqp(ErrorCondition errorCondition);
}
