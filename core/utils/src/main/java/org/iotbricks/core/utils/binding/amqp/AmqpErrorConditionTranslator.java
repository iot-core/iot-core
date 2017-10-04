package org.iotbricks.core.utils.binding.amqp;

import org.iotbricks.core.utils.binding.ErrorCondition;

public interface AmqpErrorConditionTranslator {
    public ErrorCondition fromAmqp(String errorCondition);
    public String toAmqp(ErrorCondition errorCondition);
}
